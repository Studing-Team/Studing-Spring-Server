package studing.studing_server.home.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.home.dto.MemberDataResponse;
import studing.studing_server.home.dto.UnreadCategoryResponse;
import studing.studing_server.home.dto.UnreadNoticeCountResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.dto.NoticeResponse;
import studing.studing_server.home.dto.notice.RecentNoticesResponse;
import studing.studing_server.notices.dto.SavedNoticeResponse;
import studing.studing_server.home.dto.notice.SavedNoticesResponse;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.entity.SaveNotice;
import studing.studing_server.notices.repository.NoticeLikeRepository;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notices.repository.NoticeViewRepository;
import studing.studing_server.notices.repository.SaveNoticeRepository;
import studing.studing_server.universityData.entity.CollegeDepartment;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.CollegeDepartmentRepository;
import studing.studing_server.universityData.repository.DepartmentRepository;
import studing.studing_server.universityData.repository.UniversityDataRepository;

@Service
@RequiredArgsConstructor
public class HomeService {
    private static final String S3_BUCKET_URL = "https://studing-static-files.s3.ap-northeast-2.amazonaws.com/";
    private final MemberRepository memberRepository;
    private final UniversityDataRepository universityDataRepository;
    private final CollegeDepartmentRepository collegeDepartmentRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeViewRepository noticeViewRepository;


    private  final SaveNoticeRepository saveNoticeRepository;
    private  final NoticeLikeRepository noticeLikeRepository;

    private final DepartmentRepository departmentRepository;


    public LogoResponse getLogoWithName(String loginIdentifier) {
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        University university = universityDataRepository.findByUniversityName(member.getMemberUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 대학을 찾을 수 없습니다."));

        CollegeDepartment collegeDepartment = collegeDepartmentRepository
                .findByCollegeDepartmentNameAndUniversity_UniversityName(member.getMemberCollegeDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 단과대를 찾을 수 없습니다."));

        Department department = departmentRepository.findByDepartmentNameAndUniversity_UniversityName(member.getMemberDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 학과를 찾을 수 없습니다."));

        String universityLogoUrl = university.getUniversityLogoImage() != null ?
                S3_BUCKET_URL + university.getUniversityLogoImage() : null;

        String collegeDepartmentLogoUrl = collegeDepartment.getCollegeDepartmentLogoImage() != null ?
                S3_BUCKET_URL + collegeDepartment.getCollegeDepartmentLogoImage() : null;

        String departmentLogoUrl = department.getDepartmentImage() != null ?
                S3_BUCKET_URL + department.getDepartmentImage() : null;

        return new LogoResponse(
                universityLogoUrl,
                "총학생회",
                collegeDepartmentLogoUrl,
                collegeDepartment.getCollegeDepartmentName(),
                departmentLogoUrl,
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



    @Transactional(readOnly = true)
    public RecentNoticesResponse getRecentNotices(String loginIdentifier, String categorie) {

        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        List<Notice> recentNotices = noticeRepository.findTop5ByMember_MemberUniversityOrderByCreatedAtDesc(
                currentMember.getMemberUniversity()
        );

        List<NoticeResponse> filteredNotices = new ArrayList<>();

        for (Notice notice : recentNotices) {
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String writerInfo = "";

            if ("전체".equals(categorie)) {
                if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                        && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                    matches = true;
                    writerInfo = "총학생회";
                } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                        && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                    matches = true;
                    writerInfo = noticeWriter.getMemberCollegeDepartment();
                } else if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                        && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                    matches = true;
                    writerInfo = noticeWriter.getMemberDepartment();
                }
            } else {
                switch(categorie) {
                    case "총학생회":
                        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                                && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                            matches = true;
                            writerInfo = "총학생회";
                        }
                        break;

                    case "단과대":
                        if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                                && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                            matches = true;
                            writerInfo = noticeWriter.getMemberCollegeDepartment();
                        }
                        break;

                    case "학과":
                        if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                                && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                            matches = true;
                            writerInfo = noticeWriter.getMemberDepartment();
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("잘못된 카테고리입니다. '전체', '총학생회', '단과대', '학과' 중 하나를 입력해주세요.");
                }
            }

            if (matches) {
                // 이미지 처리
                String image = "";
                if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
                    String originalImage = notice.getNoticeImages().get(0).getNoticeImage();
                    image = originalImage != null ? S3_BUCKET_URL + originalImage : "";
                }

                // 저장과 좋아요 상태 확인
                boolean saveCheck = saveNoticeRepository.existsByMemberIdAndNoticeId(
                        currentMember.getId(),
                        notice.getId()
                );

                boolean likeCheck = noticeLikeRepository.existsByMemberIdAndNoticeId(
                        currentMember.getId(),
                        notice.getId()
                );

                filteredNotices.add(new NoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getContent(),
                        writerInfo,
                        notice.getNoticeLike(),
                        notice.getViewCount(),
                        notice.getSaveCount(),
                        image,
                        notice.getCreatedAt(),
                        saveCheck,
                        likeCheck
                ));
            }
        }

        return new RecentNoticesResponse(filteredNotices);
    }


    @Transactional(readOnly = true)
    public SavedNoticesResponse getSavedNotices(String loginIdentifier) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 저장한 공지사항 조회 (최근 5개)
        List<SaveNotice> savedNotices = saveNoticeRepository
                .findTop5ByMemberIdOrderByNoticeCreatedAtDesc(currentMember.getId());


        List<SavedNoticeResponse> noticeResponses = savedNotices.stream()
                .map(savedNotice -> {
                    Notice notice = savedNotice.getNotice();
                    Member noticeWriter = notice.getMember();
                    String affiliation;

                    // 작성자의 권한에 따른 소속 정보 설정
                    if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())) {
                        affiliation = "총학생회";
                    } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
                        affiliation = noticeWriter.getMemberCollegeDepartment();
                    } else {
                        affiliation = noticeWriter.getMemberDepartment();
                    }

                    return new SavedNoticeResponse(
                            notice.getId(),
                            affiliation,
                            notice.getTitle(),
                            notice.getContent(),
                            notice.getCreatedAt(),
                            true  // 저장된 공지사항 목록이므로 항상 true
                    );
                })
                .collect(Collectors.toList());

        return new SavedNoticesResponse(noticeResponses);
    }







}
