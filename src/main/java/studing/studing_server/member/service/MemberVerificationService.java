package studing.studing_server.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.entity.MemberStatus;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notification.service.NotificationService;


@Slf4j
@Service
@RequiredArgsConstructor
public class MemberVerificationService {

    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public void verifyMember(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        member.setRole(role);
        memberRepository.save(member);

        switch (role) {
            case "ROLE_USER" -> sendVerificationNotification(member, NotificationType.USER);
            case "ROLE_UNIVERSITY" -> sendVerificationNotification(member, NotificationType.UNIVERSITY);
            case "ROLE_COLLEGE" -> sendVerificationNotification(member, NotificationType.COLLEGE);
            case "ROLE_DEPARTMENT" -> sendVerificationNotification(member, NotificationType.DEPARTMENT);
            case "ROLE_DENY" -> sendVerificationNotification(member, NotificationType.DENIED);
        }
    }

    private enum NotificationType {
        USER("학교 인증 완료", "%s님의 학교 인증이 완료되었습니다. 이제 Studing의 모든 서비스를 이용하실 수 있습니다."),
        UNIVERSITY("총학생회 권한 부여", "%s님께 총학생회 권한이 부여되었습니다. 이제 총학생회 공지사항을 작성하실 수 있습니다."),
        COLLEGE("단과대학 학생회 권한 부여", "%s님께 단과대학 학생회 권한이 부여되었습니다. 이제 단과대학 공지사항을 작성하실 수 있습니다."),
        DEPARTMENT("학과 학생회 권한 부여", "%s님께 학과 학생회 권한이 부여되었습니다. 이제 학과 공지사항을 작성하실 수 있습니다."),
        DENIED("학교 인증 거절", "%s님의 학교 인증이 거절되었습니다. 관리자에게 문의해주세요.");

        private final String title;
        private final String messageFormat;

        NotificationType(String title, String messageFormat) {
            this.title = title;
            this.messageFormat = messageFormat;
        }
    }

    private void sendVerificationNotification(Member member, NotificationType type) {
        String body = String.format(type.messageFormat, member.getName());
        log.info("Sending verification notification - MemberId: {}, MemberName: {}, NotificationType: {}, Title: {}, Body: {}",
                member.getId(),
                member.getName(),
                type.name(),
                type.title,
                body
        );

        try {
            notificationService.sendNotificationToMember(
                    member.getId(),
                    type.title,
                    body
            );
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}
