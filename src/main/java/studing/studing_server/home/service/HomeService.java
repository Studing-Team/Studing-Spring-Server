package studing.studing_server.home.service;

import java.time.LocalDateTime;
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
import studing.studing_server.home.dto.UnreadNoticeCountResponse;
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

        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<Notice> recentNotices = noticeRepository.findAllByMemberUniversityAndCreatedAtAfter(
                currentMember.getMemberUniversity(),
                oneWeekAgo);

        Set<String> unreadCategories = new HashSet<>();

        for (Notice notice : recentNotices) {
            Optional<NoticeView> noticeView = noticeViewRepository.findByNoticeAndMember(notice, currentMember);

            boolean hasUnread;
            if (noticeView.isPresent()) {
                hasUnread = noticeView.get().isReadAt();
            } else {
                hasUnread = true;
            }

            if (!hasUnread) {
                Member noticeWriter = notice.getMember();

                switch (noticeWriter.getRole()) {
                    case "ROLE_UNIVERSITY":
                        unreadCategories.add("총학생회");
                        break;
                    case "ROLE_COLLEGE":
                        unreadCategories.add(noticeWriter.getMemberCollegeDepartment());
                        break;
                    case "ROLE_DEPARTMENT":
                        unreadCategories.add(noticeWriter.getMemberDepartment());
                        break;
                }
            }
        }


        List<String> categoryList = new ArrayList<>(unreadCategories);
        return new UnreadCategoryResponse(categoryList);
    }

    @Transactional(readOnly = true)
    public UnreadNoticeCountResponse getUnreadNoticeCount(String loginIdentifier, String categorie) {
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<Notice> recentNotices = noticeRepository.findAllByMemberUniversityAndCreatedAtAfter(
                currentMember.getMemberUniversity(),
                oneWeekAgo);

        long count = 0;

        for (Notice notice : recentNotices) {
            Optional<NoticeView> noticeView = noticeViewRepository.findByNoticeAndMember(notice, currentMember);

            boolean hasUnread;
            if (noticeView.isPresent()) {
                hasUnread = noticeView.get().isReadAt();
            } else {
                hasUnread = true;
            }

            if (!hasUnread) {
                Member noticeWriter = notice.getMember();

                switch(categorie) {
                    case "총학생회":
                        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                                && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                            count++;
                        }
                        break;

                    case "단과대":
                        if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                                && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                            count++;
                        }
                        break;

                    case "학과":
                        if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                                && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                            count++;
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("잘못된 카테고리입니다. '총학생회', '단과대', '학과' 중 하나를 입력해주세요.");
                }
            }
        }

        return new UnreadNoticeCountResponse(count);
    }









}
