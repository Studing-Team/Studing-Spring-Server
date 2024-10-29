package studing.studing_server.notices.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeImage;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.repository.NoticeImageRepository;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notices.repository.NoticeViewRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private  final NoticeViewRepository noticeViewRepository;
    private  final NoticeImageRepository noticeImageRepository;
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

}
