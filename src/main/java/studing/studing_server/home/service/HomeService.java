package studing.studing_server.home.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.home.dto.MemberDataResponse;
import studing.studing_server.home.dto.UnreadCategoryResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notices.repository.NoticeViewRepository;
import studing.studing_server.universityData.entity.CollegeDepartment;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.CollegeDepartmentRepository;
import studing.studing_server.universityData.repository.DepartmentRepository;
import studing.studing_server.universityData.repository.UniversityDataRepository;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;
    private final UniversityDataRepository universityDataRepository;
    private final CollegeDepartmentRepository collegeDepartmentRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeViewRepository noticeViewRepository;

    private final DepartmentRepository departmentRepository;


    public LogoResponse getLogoWithName(String loginIdentifier) {
        // Member 테이블에서 사용자의 memberUniversity, memberCollegeDepartment, memberDepartment 정보 조회
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // University에서 universityName에 해당하는 정보 조회
        University university = universityDataRepository.findByUniversityName(member.getMemberUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 대학을 찾을 수 없습니다."));

        // CollegeDepartment에서 collegeDepartmentName에 해당하는 정보 조회
        CollegeDepartment collegeDepartment = collegeDepartmentRepository
                .findByCollegeDepartmentNameAndUniversity_UniversityName(member.getMemberCollegeDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 단과대를 찾을 수 없습니다."));

        // Department에서 memberDepartment에 해당하는 정보 조회
        Department department = departmentRepository.findByDepartmentNameAndUniversity_UniversityName(member.getMemberDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 학과를 찾을 수 없습니다."));


        return new LogoResponse(
                university.getUniversityLogoImage(),
                "총학생회",
                collegeDepartment.getCollegeDepartmentLogoImage(),
                collegeDepartment.getCollegeDepartmentName(),
                department.getDepartmentImage(),
                department.getDepartmentName()
        );
    }

    public MemberDataResponse getMyData(String loginIdentifier) {
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return new MemberDataResponse(
                member.getAdmissionNumber(),
                member.getName(),
                member.getMemberUniversity(),
                member.getMemberDepartment(),
                member.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UnreadCategoryResponse getUnreadCategories(String loginIdentifier) {
        // 1. 현재 로그인한 사용자 정보 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2. 현재 사용자와 같은 대학교의 모든 공지사항 조회 (수정된 부분)
        List<Notice> allNotices = noticeRepository.findAllByMemberUniversity(
                currentMember.getMemberUniversity());
        // 3. 읽지 않은 공지사항이 있는 카테고리(단과대) 목록 수집
        Set<String> unreadCategories = new HashSet<>();

        for (Notice notice : allNotices) {
            // 해당 공지사항에 대한 사용자의 조회 기록 확인
            Optional<NoticeView> noticeView = noticeViewRepository.findByNoticeAndMember(notice, currentMember);

            boolean hasUnread;
            if (noticeView.isPresent()) {
                hasUnread = noticeView.get().isReadAt();
            } else {
                hasUnread = true;
            }

            if (!hasUnread) {
                Member noticeWriter = notice.getMember();

                // 같은 대학교인 경우
                if (currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                    unreadCategories.add("총학생회");
                }

                // 같은 단과대학인 경우
                if (currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                    unreadCategories.add(noticeWriter.getMemberCollegeDepartment());
                }

                // 같은 학과인 경우
                if (currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                    unreadCategories.add(noticeWriter.getMemberDepartment());
                }
            }
        }


        List<String> categoryList = new ArrayList<>(unreadCategories);
        return new UnreadCategoryResponse(categoryList);
    }









}
