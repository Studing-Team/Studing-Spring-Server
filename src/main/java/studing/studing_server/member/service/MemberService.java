package studing.studing_server.member.service;

import com.amplitude.Amplitude;
import com.amplitude.Event;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.common.exception.message.BusinessException;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.CheckLoginIdRequest;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.dto.MemberResubmitRequest;
import studing.studing_server.member.dto.SignUpResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.entity.MemberStatus;
import studing.studing_server.member.entity.WithdrawnMember;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.member.repository.WithdrawnMemberRepository;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notices.repository.NoticeViewRepository;
import studing.studing_server.slack.SlackNotificationService;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.repository.DepartmentRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final S3Service s3Service;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SlackNotificationService slackNotificationService;
    private final Amplitude amplitudeClient;
    private final NoticeRepository noticeRepository;        // 추가
    private final NoticeViewRepository noticeViewRepository; // 추가

    private static final String S3_BASE_URL = "https://studing-static-files.s3.ap-northeast-2.amazonaws.com/";

    @PostConstruct
    public void initAmplitude() {
        amplitudeClient.init("YOUR_AMPLITUDE_API_KEY");
    }

    @Transactional
    public SignUpResponse signUp(MemberCreateRequest memberCreateRequest) {
        String imageUrl = uploadStudentCardImage(memberCreateRequest.studentCardImage());

        // departmentName과 memberUniversity 기반으로 department 조회
        Department department = departmentRepository
                .findByDepartmentNameAndUniversity_UniversityName(
                        memberCreateRequest.memberDepartment(),
                        memberCreateRequest.memberUniversity()
                )
                .orElseThrow(() -> new IllegalArgumentException("사용자가 입력한 학과 또는 대학이 잘못되었습니다."));

        // 조회한 department로부터 collegeDepartmentName 조회
        String memberCollegeDepartment = department.getCollegeDepartment().getCollegeDepartmentName();

        Member member = createMember(memberCreateRequest, imageUrl, memberCollegeDepartment);

        Member savedMember = memberRepository.save(member);

        // 앰플리튜드 이벤트 로깅 추가
        logSignUpEventToAmplitude(savedMember);
        // 기존 공지사항들에 대한 NoticeView 생성
        createNoticeViewsForNewMember(savedMember);


        slackNotificationService.sendMemberVerificationRequest(member, imageUrl);

        return new SignUpResponse(savedMember.getId());
        }



    private void createNoticeViewsForNewMember(Member member) {
        // 해당 대학교의 모든 공지사항 조회
        List<Notice> existingNotices = noticeRepository.findByMember_MemberUniversityOrderByCreatedAtDesc(
                member.getMemberUniversity()
        );

        // 각 공지사항에 대한 NoticeView 생성
        for (Notice notice : existingNotices) {
            NoticeView noticeView = NoticeView.builder()
                    .notice(notice)
                    .member(member)
                    .readAt(false)
                    .build();
            noticeViewRepository.save(noticeView);
        }

        log.info("Created {} NoticeViews for new member with ID: {}",
                existingNotices.size(), member.getId());
    }

    private void logSignUpEventToAmplitude(Member member) {
        try {
            Event amplitudeEvent = new Event("Sign Up", member.getLoginIdentifier());

            JSONObject userProps = new JSONObject();
            userProps.put("id", member.getId());
            userProps.put("admission_number", member.getAdmissionNumber());
            userProps.put("name", member.getName());
            userProps.put("student_number", member.getStudentNumber());
            userProps.put("login_identifier", member.getLoginIdentifier());
            userProps.put("student_card_image", S3_BASE_URL+member.getStudentCardImage());
            userProps.put("university", member.getMemberUniversity());
            userProps.put("college_department", member.getMemberCollegeDepartment());
            userProps.put("department", member.getMemberDepartment());
            userProps.put("role", member.getRole());
            userProps.put("marketing_agreement", member.getMarketingAgreement());
            userProps.put("created_at", member.getCreatedAt());

            amplitudeEvent.userProperties = userProps;
            amplitudeClient.logEvent(amplitudeEvent);

        } catch (JSONException e) {
            log.error("Failed to log Amplitude event", e);
        }
    }

    private String uploadStudentCardImage(MultipartFile studentCardImage) {
        try {
            return s3Service.uploadImage("student-card-images/", studentCardImage);
        } catch (IOException e) {
            log.error("이미지 업로드에 실패했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다!", e);
        }
    }

    private Member createMember(MemberCreateRequest request, String imageUrl, String memberCollegeDepartment) {
        String hashedPassword = bCryptPasswordEncoder.encode(request.password());

        return Member.builder()
                .admissionNumber(request.admissionNumber())
                .name(request.name())
                .studentNumber(request.studentNumber())
                .loginIdentifier(request.loginIdentifier())
                .password(hashedPassword)
                .studentCardImage(imageUrl)
                .memberUniversity(request.memberUniversity())
                .memberDepartment(request.memberDepartment())
                .memberCollegeDepartment(memberCollegeDepartment)
                .role("ROLE_UNUSER")
                .marketingAgreement(request.marketingAgreement()) // 마케팅 동의 여부 추가
                .build();
    }







    public boolean isDuplicateEmail(CheckLoginIdRequest checkLoginIdRequest) {
        return memberRepository.existsByLoginIdentifier(checkLoginIdRequest.loginIdentifier());
    }





    public void withdrawMember(String loginIdentifier) {
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 회원 정보를 WithdrawnMember 테이블로 이동
        WithdrawnMember withdrawnMember = WithdrawnMember.builder()
                .member(member)
                .build();

        withdrawnMemberRepository.save(withdrawnMember);

        // 기존 회원 정보 삭제
        memberRepository.delete(member);
    }

    @Transactional
    public void resubmitStudentCard(String loginIdentifier, MemberResubmitRequest memberResubmitRequest) {
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

// ROLE_DENY 체크
        if (!"ROLE_DENY".equals(member.getRole())) {
            throw new BusinessException(ErrorMessage.RESUBMIT_NOT_ALLOWED);
        }

        String imageUrl = uploadStudentCardImage(memberResubmitRequest.studentCardImage());

        try {
            // 기존 학생증 이미지 삭제
            if (member.getStudentCardImage() != null) {
                s3Service.deleteImage(member.getStudentCardImage());
            }


            // 새로운 학생증 이미지 업로드
            String newImagePath = imageUrl;

            // 회원 정보 업데이트
            member.setStudentCardImage(newImagePath);
            member.setName(memberResubmitRequest.name());
            member.setStudentNumber(memberResubmitRequest.studentNumber());
            member.setRole(MemberStatus.UNVERIFIED.getRole()); // 미승인 상태로 변경

            memberRepository.save(member);

        } catch (IOException e) {
            throw new RuntimeException("학생증 이미지 처리 중 오류가 발생했습니다.", e);
        }

        slackNotificationService.sendMemberResubmissionRequest(member, imageUrl);



    }







}
