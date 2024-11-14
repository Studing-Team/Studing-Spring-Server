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

        if ("ROLE_USER".equals(role)) {
            sendVerificationNotification(member, true);
        } else {
            sendVerificationNotification(member, false);
        }
    }

    private void sendVerificationNotification(Member member, boolean isApproved) {
        String title = isApproved ? "학교 인증 완료" : "학교 인증 거절";
        String body = isApproved ?
                String.format("%s님의 학교 인증이 완료되었습니다. 이제 Studing의 모든 서비스를 이용하실 수 있습니다.", member.getName()) :
                String.format("%s님의 학교 인증이 거절되었습니다. 관리자에게 문의해주세요.", member.getName());

        try {
            notificationService.sendNotificationToMember(
                    member.getId(),
                    title,
                    body
            );
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }



}
