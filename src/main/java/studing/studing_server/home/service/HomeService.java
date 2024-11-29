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
import studing.studing_server.common.exception.message.BusinessException;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.home.dto.MemberDataResponse;
import studing.studing_server.home.dto.UnreadCategoryResponse;
import studing.studing_server.home.dto.UnreadNoticeCountResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.dto.NoticeResponse;
import studing.studing_server.home.dto.notice.RecentNoticesResponse;
import studing.studing_server.notices.dto.NoticeResponse3;
import studing.studing_server.notices.dto.RecentNoticesResponse3;
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




        boolean isRegisteredDepartment= memberRepository.existsByMemberUniversityAndMemberDepartmentAndRole(
                member.getMemberUniversity(),
                member.getMemberDepartment(),
                "ROLE_DEPARTMENT"
        );


        return new LogoResponse(
                universityLogoUrl,
                "총학생회",
                collegeDepartmentLogoUrl,
                collegeDepartment.getCollegeDepartmentName(),
                departmentLogoUrl,
                department.getDepartmentName(),
                isRegisteredDepartment
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
                        if (currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                            unreadCategories.add(noticeWriter.getMemberCollegeDepartment());
                        }
                        break;
                    case "ROLE_DEPARTMENT":
                        if (currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                            unreadCategories.add(noticeWriter.getMemberDepartment());
                        }
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
                    case "전체":
                        // 전체 카테고리의 경우 모든 공지사항을 카운트
                        if (("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                                && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity()))
                                || ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                                && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment()))
                                || ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                                && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment()))) {
                            count++;
                        }
                        break;

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
                        throw new BusinessException(
                                ErrorMessage.INVALID_CATEGORY
                        );
                }
            }
        }

        return new UnreadNoticeCountResponse(count);
    }


    @Transactional(readOnly = true)
    public RecentNoticesResponse3 getRecentNotices(String loginIdentifier, String categorie) {

        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
//
//        List<Notice> recentNotices = noticeRepository.findTop5ByMember_MemberUniversityOrderByCreatedAtDesc(
//                currentMember.getMemberUniversity()
//        );

        // 해당 대학의 모든 공지사항을 가져옴
        List<Notice> recentNotices = noticeRepository.findByMember_MemberUniversityOrderByCreatedAtDesc(
                currentMember.getMemberUniversity()
        );


        List<NoticeResponse3> filteredNotices = new ArrayList<>();

        for (Notice notice : recentNotices) {
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String writerInfo = "";

            if ("전체".equals(categorie)) {
                if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                        && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                    matches = true;
                    writerInfo = "총학생회[총학생회]";
                } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                        && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                    matches = true;
                    writerInfo = noticeWriter.getMemberCollegeDepartment()+"[단과대]";
                } else if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                        && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                    matches = true;
                    writerInfo = noticeWriter.getMemberDepartment()+"[학과]";
                }
            } else {
                switch(categorie) {
                    case "총학생회":
                        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                                && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                            matches = true;
                            writerInfo = "총학생회[총학생회]";
                        }
                        break;

                    case "단과대":
                        if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                                && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                            matches = true;
                            writerInfo = noticeWriter.getMemberCollegeDepartment()+"[단과대]";
                        }
                        break;

                    case "학과":
                        if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                                && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                            matches = true;
                            writerInfo = noticeWriter.getMemberDepartment()+"[학과]";
                        }
                        break;

                    default:
                        throw new BusinessException(
                                ErrorMessage.INVALID_CATEGORY
                        );
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

                filteredNotices.add(NoticeResponse3.from(
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
                        likeCheck,
                        categorie
                ));
            }
        }


        // 필터링된 공지사항 중 최근 5개만 선택
        List<NoticeResponse3> top5Notices = filteredNotices.stream()
                .limit(5)
                .collect(Collectors.toList());



        return new RecentNoticesResponse3(top5Notices);
    }


    @Transactional(readOnly = true)
    public SavedNoticesResponse getSavedNotices(String loginIdentifier) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 모든 저장된 공지사항 조회
        List<SaveNotice> savedNotices = saveNoticeRepository
                .findByMemberIdOrderByNoticeCreatedAtDesc(currentMember.getId());


        // 저장된 공지사항들을 변환
        List<SavedNoticeResponse> noticeResponses = savedNotices.stream()
                .map(savedNotice -> {
                    Notice notice = savedNotice.getNotice();
                    Member noticeWriter = notice.getMember();
                    String affiliation = "";

                    // 작성자의 권한에 따른 소속 정보 설정
                    if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())) {
                        affiliation = "총학생회[총학생회]";
                    } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
                        affiliation = noticeWriter.getMemberCollegeDepartment() + "[단과대]";
                    } else if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())) {
                        affiliation = noticeWriter.getMemberDepartment() + "[학과]";
                    }

                    return SavedNoticeResponse.from(
                            notice.getId(),
                            affiliation,
                            notice.getTitle(),
                            notice.getContent(),
                            notice.getCreatedAt(),
                            true  // 저장된 공지사항 목록이므로 항상 true
                    );
                })
                // 최근 5개만 선택
                .limit(5)
                .collect(Collectors.toList());

        return new SavedNoticesResponse(noticeResponses);
    }







}
