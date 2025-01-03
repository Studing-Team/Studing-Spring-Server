package studing.studing_server.notices.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.common.exception.message.BusinessException;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.NoticeCreateRequest;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.dto.NoticeDetailResponse;
import studing.studing_server.notices.dto.NoticeResponse;
import studing.studing_server.notices.dto.NoticeResponse2;
import studing.studing_server.home.dto.notice.RecentNoticesResponse;
import studing.studing_server.notices.dto.RecentNoticesResponse2;
import studing.studing_server.notices.dto.SavedNoticeResponse2;
import studing.studing_server.notices.dto.SavedNoticesResponse2;
import studing.studing_server.notices.dto.UnreadNoticeResponse;
import studing.studing_server.notices.dto.UnreadNoticesResponse;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeImage;
import studing.studing_server.notices.entity.NoticeLike;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.entity.SaveNotice;
import studing.studing_server.notices.repository.NoticeImageRepository;
import studing.studing_server.notices.repository.NoticeLikeRepository;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notices.repository.NoticeViewRepository;
import studing.studing_server.notices.repository.SaveNoticeRepository;
import studing.studing_server.notification.service.NotificationService;
import studing.studing_server.universityData.entity.CollegeDepartment;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.CollegeDepartmentRepository;
import studing.studing_server.universityData.repository.DepartmentRepository;
import studing.studing_server.universityData.repository.UniversityDataRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private static final String S3_BUCKET_URL = "https://studing-static-files.s3.ap-northeast-2.amazonaws.com/";

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private  final NoticeViewRepository noticeViewRepository;
    private  final NoticeImageRepository noticeImageRepository;
    private  final SaveNoticeRepository saveNoticeRepository;
    private  final NoticeLikeRepository noticeLikeRepository;
    private  final CollegeDepartmentRepository collegeDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityDataRepository universityDataRepository;
    private final NotificationService notificationService;

    private final S3Service s3Service;

    @Transactional
    public void createPost(NoticeCreateRequest noticeCreateRequest) {
        Member member = getAuthenticatedMember();

        Notice notice = saveNotice(noticeCreateRequest, member);
        saveNoticeImages(noticeCreateRequest, notice);

        // NoticeView 생성 및 알림 발송을 위한 대상 멤버 조회
        List<Member> targetMembers = getTargetMembers(member);

        // NoticeView 생성
        createNoticeViews(notice, targetMembers);

        // 알림 발송
        sendNotifications(member,noticeCreateRequest.title(), targetMembers, notice.getId());

    }


    private List<Member> getTargetMembers(Member noticeWriter) {
        switch (noticeWriter.getRole()) {
            case "ROLE_UNIVERSITY" -> {
                return memberRepository.findByMemberUniversity(noticeWriter.getMemberUniversity());
            }
            case "ROLE_COLLEGE" -> {
                return memberRepository.findByMemberUniversityAndMemberCollegeDepartment(
                        noticeWriter.getMemberUniversity(),
                        noticeWriter.getMemberCollegeDepartment()
                );
            }
            case "ROLE_DEPARTMENT" -> {
                return memberRepository.findByMemberUniversityAndMemberDepartment(
                        noticeWriter.getMemberUniversity(),
                        noticeWriter.getMemberDepartment()
                );
            }
            default -> {
                log.warn("Unexpected role for notice writer: {}", noticeWriter.getRole());
                return List.of();
            }
        }
    }



    private Member getAuthenticatedMember() {

        return memberRepository.findByLoginIdentifier(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    private Notice saveNotice(NoticeCreateRequest request, Member member) {
        Notice notice = Notice.builder()
                .title(request.title())
                .tag(request.tag())
                .content(request.content())
                .member(member)
                .build();
        noticeRepository.save(notice);
        return notice;
    }

    private void saveNoticeImages(NoticeCreateRequest request, Notice notice) {
        if (request.noticeImages() != null) {
            for (MultipartFile file : request.noticeImages()) {
                String fileName = storeFile(file);
                NoticeImage noticeImage = NoticeImage.builder()
                        .notice(notice)
                        .noticeImage(fileName)
                        .build();
                notice.addNoticeImage(noticeImage);
                noticeImageRepository.save(noticeImage);
            }
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            return s3Service.uploadImage("notice-images/", file);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }



    private void createNoticeViews(Notice notice, List<Member> targetMembers) {
        List<NoticeView> noticeViews = targetMembers.stream()
                .map(member -> NoticeView.builder()
                        .notice(notice)
                        .member(member)
                        .readAt(false)
                        .build())
                .toList();

        noticeViewRepository.saveAll(noticeViews);
    }

    private void sendNotifications(Member writer,String noticeTitle, List<Member> targetMembers,Long noticeId) {
//        String title = "새로운 공지사항";
        String title = switch (writer.getRole()) {
            case "ROLE_UNIVERSITY" -> "총학생회의 새로운 공지를 확인하세요.";
            case "ROLE_COLLEGE" -> writer.getMemberCollegeDepartment() + "의 새로운 공지를 확인하세요.";
            case "ROLE_DEPARTMENT" -> writer.getMemberDepartment() + "의 새로운 공지를 확인하세요.";
            default -> throw new IllegalArgumentException("Invalid role: " + writer.getRole());
        };

        Map<String, String> data = new HashMap<>();
        data.put("noticeId", noticeId.toString());
        data.put("type", "NOTICE");
        for (Member targetMember : targetMembers) {
            try {
                notificationService.sendNotificationToMember(
                        targetMember.getId(),
                        title,
                        noticeTitle,
                        data
                );
            } catch (Exception e) {
                log.error("Failed to send notification to member {}: {}",
                        targetMember.getId(), e.getMessage());
            }
        }
    }


    @Transactional(readOnly = true)
    public RecentNoticesResponse getAllNotices(String loginIdentifier) {
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        List<Notice> allNotices = noticeRepository.findByMember_MemberUniversityOrderByCreatedAtDesc(
                currentMember.getMemberUniversity()
        );

        List<NoticeResponse> filteredNotices = new ArrayList<>();

        for (Notice notice : allNotices) {
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String writerInfo = "";

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

                filteredNotices.add(NoticeResponse.from(
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
    public RecentNoticesResponse2 getAllCategoryNotices(String loginIdentifier, String categorie) {
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        List<Notice> recentNotices = noticeRepository.findByMember_MemberUniversityOrderByCreatedAtDesc(
                currentMember.getMemberUniversity()
        );

        List<NoticeResponse2> filteredNotices = new ArrayList<>();

        for (Notice notice : recentNotices) {
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String writerInfo = "";

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
                    throw new BusinessException(
                            ErrorMessage.INVALID_CATEGORY
                    );
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

                filteredNotices.add(NoticeResponse2.from(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getContent(),
                        notice.getTag(),
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

        return new RecentNoticesResponse2(filteredNotices);
    }



    @Transactional
    public void saveNotice(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 이미 저장한 공지인지 확인
        if (saveNoticeRepository.existsByMemberIdAndNoticeId(currentMember.getId(), noticeId)) {
            throw new IllegalStateException("이미 저장한 공지사항입니다.");
        }

        // SaveNotice 생성 및 저장
        SaveNotice saveNotice = SaveNotice.builder()
                .notice(notice)
                .member(currentMember)
                .build();
        saveNoticeRepository.save(saveNotice);

        // 공지사항의 저장 수 증가
        notice.setSaveCount(notice.getSaveCount() + 1);
        noticeRepository.save(notice);
    }

    // NoticeService에 추가
    @Transactional
    public void cancelSaveNotice(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 저장한 공지인지 확인
        SaveNotice saveNotice = saveNoticeRepository.findByMemberIdAndNoticeId(currentMember.getId(), noticeId)
                .orElseThrow(() -> new IllegalStateException("저장하지 않은 공지사항입니다."));

        // SaveNotice 삭제
        saveNoticeRepository.delete(saveNotice);

        // 공지사항의 저장 수 감소
        notice.setSaveCount(notice.getSaveCount() - 1);
        noticeRepository.save(notice);
    }

    // NoticeService에 추가
    @Transactional
    public void likeNotice(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 이미 좋아요한 공지인지 확인
        if (noticeLikeRepository.existsByMemberIdAndNoticeId(currentMember.getId(), noticeId)) {
            throw new IllegalStateException("이미 좋아요한 공지사항입니다.");
        }

        // NoticeLike 생성 및 저장
        NoticeLike noticeLike = NoticeLike.builder()
                .notice(notice)
                .member(currentMember)
                .build();
        noticeLikeRepository.save(noticeLike);

        // 공지사항의 좋아요 수 증가
        notice.setNoticeLike(notice.getNoticeLike() + 1);
        noticeRepository.save(notice);
    }

    @Transactional
    public void cancelLikeNotice(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 좋아요한 공지인지 확인
        NoticeLike noticeLike = noticeLikeRepository.findByMemberIdAndNoticeId(currentMember.getId(), noticeId)
                .orElseThrow(() -> new IllegalStateException("좋아요하지 않은 공지사항입니다."));

        // NoticeLike 삭제
        noticeLikeRepository.delete(noticeLike);

        // 공지사항의 좋아요 수 감소
        notice.setNoticeLike(notice.getNoticeLike() - 1);
        noticeRepository.save(notice);
    }
    // NoticeService에 추가

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        Member noticeWriter = notice.getMember();

        // 작성자의 소속 정보와 로고 이미지 가져오기
        String affiliationName;
        String logoImage;

        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())) {
            University university = universityDataRepository.findByUniversityName(noticeWriter.getMemberUniversity())
                    .orElseThrow(() -> new IllegalArgumentException("대학교 정보를 찾을 수 없습니다."));

            affiliationName = university.getUniversityNickName();
            String originalLogo = university.getUniversityLogoImage();
            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
        } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
            CollegeDepartment collegeDepartment = collegeDepartmentRepository
                    .findByCollegeDepartmentNameAndUniversity_UniversityName(
                            noticeWriter.getMemberCollegeDepartment(),
                            noticeWriter.getMemberUniversity()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("단과대학 정보를 찾을 수 없습니다."));
            affiliationName = collegeDepartment.getCollegeDepartmentNickName();
            String originalLogo = collegeDepartment.getCollegeDepartmentLogoImage();
            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
        } else {
            Department department = departmentRepository
                    .findByDepartmentNameAndUniversity_UniversityName(
                            noticeWriter.getMemberDepartment(),
                            noticeWriter.getMemberUniversity()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("학과 정보를 찾을 수 없습니다."));
            affiliationName = department.getDepartmentNickName();
            String originalLogo = department.getDepartmentImage();
            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
        }

        // 저장 여부 확인
        boolean saveCheck = saveNoticeRepository.existsByMemberIdAndNoticeId(
                currentMember.getId(),
                noticeId
        );

        // 좋아요 여부 확인
        boolean likeCheck = noticeLikeRepository.existsByMemberIdAndNoticeId(
                currentMember.getId(),
                noticeId
        );

        // 이미지 URL 리스트 생성 (S3 버킷 URL 추가)
        List<String> images = notice.getNoticeImages().stream()
                .map(NoticeImage::getNoticeImage)
                .map(image -> image != null ? S3_BUCKET_URL + image : "")
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());

        return NoticeDetailResponse.from(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getNoticeLike(),
                notice.getSaveCount(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                affiliationName,
                logoImage,
                notice.getTag(),
                images,
                saveCheck,
                likeCheck
        );
    }


    @Transactional(readOnly = true)
    public SavedNoticesResponse2 getSavedNoticesByCategory(String loginIdentifier, String categorie) {
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        List<SaveNotice> savedNotices = saveNoticeRepository
                .findByMemberIdOrderByNoticeCreatedAtDesc(currentMember.getId());

        List<SavedNoticeResponse2> noticeResponses = new ArrayList<>();

        for (SaveNotice savedNotice : savedNotices) {
            Notice notice = savedNotice.getNotice();
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String affiliation = "";

            if ("전체".equals(categorie)) {
                matches = true;
                if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())) {
                    affiliation = "총학생회";
                } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
                    affiliation = noticeWriter.getMemberCollegeDepartment();
                } else if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())) {
                    affiliation = noticeWriter.getMemberDepartment();
                }
            } else {
                switch(categorie) {
                    case "총학생회":
                        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())) {
                            matches = true;
                            affiliation = "총학생회";
                        }
                        break;

                    case "단과대":
                        if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
                            matches = true;
                            affiliation = noticeWriter.getMemberCollegeDepartment();
                        }
                        break;

                    case "학과":
                        if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())) {
                            matches = true;
                            affiliation = noticeWriter.getMemberDepartment();
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

                noticeResponses.add(SavedNoticeResponse2.of(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getCreatedAt(),
                        image,  // 이미지 URL 추가
                        true  // 저장된 공지사항이므로 항상 true
                ));
            }
        }

        return new SavedNoticesResponse2(noticeResponses);
    }



    @Transactional
    public boolean checkNoticeView(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // NoticeView 조회
        NoticeView noticeView = noticeViewRepository.findByMemberIdAndNoticeId(currentMember.getId(), noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항의 조회 기록이 없습니다."));

        // readAt이 false인 경우에만 처리
        if (!noticeView.isReadAt()) {
            // readAt을 true로 업데이트
            noticeView.setReadAt(true);
            // 공지사항의 조회수 증가
            notice.setViewCount(notice.getViewCount() + 1);
            return true; // 조회수가 증가되었음을 반환
        }
        return false; // 이미 읽은 상태라 조회수가 증가하지 않았음을 반환
    }



    @Transactional(readOnly = true)
    public UnreadNoticesResponse getAllUnreadNotices(String loginIdentifier, String categorie) {
        if (!Arrays.asList("전체", "총학생회", "단과대", "학과").contains(categorie)) {
            throw new BusinessException(ErrorMessage.INVALID_CATEGORY);
        }


        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        List<Notice> notices = noticeRepository.findAllByMemberUniversityAndCreatedAtAfter(
                currentMember.getMemberUniversity(),
                oneWeekAgo);


        List<UnreadNoticeResponse> unreadNotices = new ArrayList<>();

        for (Notice notice : notices) {
            Member noticeWriter = notice.getMember();
            boolean matches = false;
            String affiliationName = "";
            String logoImage = "";

            Optional<NoticeView> noticeView = noticeViewRepository.findByMemberIdAndNoticeId(
                    currentMember.getId(),
                    notice.getId()
            );

            boolean isUnread = noticeView.map(nv -> !nv.isReadAt()).orElse(true);

            if (!isUnread) {
                continue;
            }

            if ("전체".equals(categorie)) {
                if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                        && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                    matches = true;
                    affiliationName = "총학생회";
                    String originalLogo = getUniversityLogo(noticeWriter);
                    logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                        && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                    matches = true;
                    affiliationName = noticeWriter.getMemberCollegeDepartment();
                    String originalLogo = getCollegeLogo(noticeWriter);
                    logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                } else if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                        && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                    matches = true;
                    affiliationName = noticeWriter.getMemberDepartment();
                    String originalLogo = getDepartmentLogo(noticeWriter);
                    logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                }
            } else {
                switch(categorie) {
                    case "총학생회":
                        if ("ROLE_UNIVERSITY".equals(noticeWriter.getRole())
                                && currentMember.getMemberUniversity().equals(noticeWriter.getMemberUniversity())) {
                            matches = true;
                            affiliationName = "총학생회";
                            String originalLogo = getUniversityLogo(noticeWriter);
                            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                        }
                        break;

                    case "단과대":
                        if ("ROLE_COLLEGE".equals(noticeWriter.getRole())
                                && currentMember.getMemberCollegeDepartment().equals(noticeWriter.getMemberCollegeDepartment())) {
                            matches = true;
                            affiliationName = noticeWriter.getMemberCollegeDepartment();
                            String originalLogo = getCollegeLogo(noticeWriter);
                            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                        }
                        break;

                    case "학과":
                        if ("ROLE_DEPARTMENT".equals(noticeWriter.getRole())
                                && currentMember.getMemberDepartment().equals(noticeWriter.getMemberDepartment())) {
                            matches = true;
                            affiliationName = noticeWriter.getMemberDepartment();
                            String originalLogo = getDepartmentLogo(noticeWriter);
                            logoImage = originalLogo != null ? S3_BUCKET_URL + originalLogo : "";
                        }
                        break;
                }
            }

            if (matches) {
                // 공지사항 이미지들에 S3 URL 추가
                List<String> images = notice.getNoticeImages().stream()
                        .map(NoticeImage::getNoticeImage)
                        .map(image -> image != null ? S3_BUCKET_URL + image : "")
                        .filter(url -> !url.isEmpty())
                        .collect(Collectors.toList());


                // 저장 여부 확인
                boolean saveCheck = saveNoticeRepository.existsByMemberIdAndNoticeId(
                        currentMember.getId(),
                        notice.getId()
                );
                // 좋아요 여부 확인
                boolean likeCheck = noticeLikeRepository.existsByMemberIdAndNoticeId(
                        currentMember.getId(),
                        notice.getId()
                );


                unreadNotices.add(UnreadNoticeResponse.from(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getContent(),
                        notice.getNoticeLike(),
                        notice.getSaveCount(),
                        notice.getViewCount(),
                        notice.getCreatedAt(),
                        affiliationName,
                        logoImage,
                        notice.getTag(),
                        images,
                        saveCheck,
                        likeCheck
                ));
            }
        }

        return new UnreadNoticesResponse(unreadNotices);
    }






    // 로고 이미지 조회 헬퍼 메서드들
    private String getUniversityLogo(Member writer) {
        return universityDataRepository.findByUniversityName(writer.getMemberUniversity())
                .map(University::getUniversityLogoImage)
                .orElse("");
    }

    private String getCollegeLogo(Member writer) {
        return collegeDepartmentRepository
                .findByCollegeDepartmentNameAndUniversity_UniversityName(
                        writer.getMemberCollegeDepartment(),
                        writer.getMemberUniversity())
                .map(CollegeDepartment::getCollegeDepartmentLogoImage)
                .orElse("");
    }

    private String getDepartmentLogo(Member writer) {
        return departmentRepository
                .findByDepartmentNameAndUniversity_UniversityName(
                        writer.getMemberDepartment(),
                        writer.getMemberUniversity())
                .map(Department::getDepartmentImage)
                .orElse("");
    }


    @Transactional
    public void deleteNotice(String loginIdentifier, Long noticeId) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 작성자 확인
        if (!notice.getMember().getId().equals(currentMember.getId())) {
            throw new IllegalStateException("해당 공지사항의 작성자만 삭제할 수 있습니다.");
        }

        // S3에서 이미지 삭제
        if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
            for (NoticeImage image : notice.getNoticeImages()) {
                try {
                    s3Service.deleteImage(image.getNoticeImage());
                } catch (IOException e) {
                    log.error("이미지 삭제 실패: {}", e.getMessage());
                }
            }
        }

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }






    @Transactional
    public void updateNotice(String loginIdentifier, Long noticeId, NoticeCreateRequest updateRequest) {
        // 현재 사용자 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 작성자 확인
        if (!notice.getMember().getId().equals(currentMember.getId())) {
            throw new IllegalStateException("해당 공지사항의 작성자만 수정할 수 있습니다.");
        }

        // 기존 이미지 삭제
        if (updateRequest.noticeImages() != null && !updateRequest.noticeImages().isEmpty()) {
            // 기존 이미지 S3에서 삭제
            for (NoticeImage image : notice.getNoticeImages()) {
                try {
                    s3Service.deleteImage(image.getNoticeImage());
                } catch (IOException e) {
                    log.error("이미지 삭제 실패: {}", e.getMessage());
                }
            }
            notice.getNoticeImages().clear();

            // 새 이미지 업로드 및 저장
            for (MultipartFile file : updateRequest.noticeImages()) {
                String fileName = storeFile(file);
                NoticeImage noticeImage = NoticeImage.builder()
                        .notice(notice)
                        .noticeImage(fileName)
                        .build();
                notice.addNoticeImage(noticeImage);
                noticeImageRepository.save(noticeImage);
            }
        }

        // 공지사항 내용 업데이트
        notice.setTitle(updateRequest.title());
        notice.setContent(updateRequest.content());
        notice.setTag(updateRequest.tag());

        noticeRepository.save(notice);
    }







}
