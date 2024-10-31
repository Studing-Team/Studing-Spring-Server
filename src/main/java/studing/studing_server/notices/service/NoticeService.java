package studing.studing_server.notices.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.CustomMemberDetails;
import studing.studing_server.member.dto.NoticeCreateRequest;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.dto.NoticeDetailResponse;
import studing.studing_server.notices.dto.NoticeResponse;
import studing.studing_server.notices.dto.NoticeResponse2;
import studing.studing_server.notices.dto.RecentNoticesResponse;
import studing.studing_server.notices.dto.RecentNoticesResponse2;
import studing.studing_server.notices.dto.SavedNoticeResponse;
import studing.studing_server.notices.dto.SavedNoticesResponse;
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
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private  final NoticeViewRepository noticeViewRepository;
    private  final NoticeImageRepository noticeImageRepository;
    private  final SaveNoticeRepository saveNoticeRepository;
    private  final NoticeLikeRepository noticeLikeRepository;
    private  final CollegeDepartmentRepository collegeDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final UniversityDataRepository universityDataRepository;

    private final S3Service s3Service;

    @Transactional
    public void createPost(NoticeCreateRequest noticeCreateRequest) {
        Member member = getAuthenticatedMember();

        Notice notice = saveNotice(noticeCreateRequest, member);
        saveNoticeImages(noticeCreateRequest, notice);
        createNoticeViews(notice, member.getMemberUniversity());
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

    private void createNoticeViews(Notice notice, String universityName) {
        List<Member> universityMembers = memberRepository.findByMemberUniversity(universityName);
        for (Member universityMember : universityMembers) {
            NoticeView noticeView = NoticeView.builder()
                    .notice(notice)
                    .member(universityMember)
                    .readAt(false)
                    .build();
            noticeViewRepository.save(noticeView);
        }
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
                    image = notice.getNoticeImages().get(0).getNoticeImage();
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

            if (matches) {
                // 이미지 처리
                String image = "";
                if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
                    image = notice.getNoticeImages().get(0).getNoticeImage();
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
                        throw new IllegalArgumentException("잘못된 카테고리입니다. '전체', '총학생회', '단과대', '학과' 중 하나를 입력해주세요.");
                }


            if (matches) {
                // 이미지 처리
                String image = "";
                if (notice.getNoticeImages() != null && !notice.getNoticeImages().isEmpty()) {
                    image = notice.getNoticeImages().get(0).getNoticeImage();
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

                filteredNotices.add(new NoticeResponse2(
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
            logoImage = university.getUniversityLogoImage();
        } else if ("ROLE_COLLEGE".equals(noticeWriter.getRole())) {
            CollegeDepartment collegeDepartment = collegeDepartmentRepository
                    .findByCollegeDepartmentNameAndUniversity_UniversityName(
                            noticeWriter.getMemberCollegeDepartment(),
                            noticeWriter.getMemberUniversity()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("단과대학 정보를 찾을 수 없습니다."));
            affiliationName = collegeDepartment.getCollegeDepartmentNickName();
            logoImage = collegeDepartment.getCollegeDepartmentLogoImage();
        } else {
            Department department = departmentRepository
                    .findByDepartmentNameAndUniversity_UniversityName(
                            noticeWriter.getMemberDepartment(),
                            noticeWriter.getMemberUniversity()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("학과 정보를 찾을 수 없습니다."));
            affiliationName = department.getDepartmentNickName();
            logoImage = department.getDepartmentImage();
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

        // 이미지 URL 리스트 생성
        List<String> images = notice.getNoticeImages().stream()
                .map(NoticeImage::getNoticeImage)
                .collect(Collectors.toList());

        return new NoticeDetailResponse(
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



    // NoticeService에 추가
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
