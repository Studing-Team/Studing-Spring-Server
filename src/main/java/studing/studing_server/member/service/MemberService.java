package studing.studing_server.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.CheckLoginIdRequest;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.dto.SignUpResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
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

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SlackNotificationService slackNotificationService;





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



        slackNotificationService.sendMemberVerificationRequest(member, imageUrl);

        return new SignUpResponse(savedMember.getId());
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














}
