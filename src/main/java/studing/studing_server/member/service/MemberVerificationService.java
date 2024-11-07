package studing.studing_server.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.entity.MemberStatus;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
public class MemberVerificationService {

    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public void verifyMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 회원 상태 및 역할 업데이트
        member.setRole(MemberStatus.VERIFIED.getRole());
        memberRepository.save(member);

        // 인증 완료 알림 발송
        sendVerificationNotification(member);
    }

    private void sendVerificationNotification(Member member) {
        String title = "학교 인증 완료";
        String body = String.format("%s님의 학교 인증이 완료되었습니다. 이제 Studing의 모든 서비스를 이용하실 수 있습니다.",
                member.getName());

        try {
            notificationService.sendNotificationToMember(
                    member.getId(),
                    title,
                    body
            );
        } catch (Exception e) {
            // 알림 발송 실패 로깅
            // 알림 발송 실패가 전체 트랜잭션을 롤백시키지 않도록 예외 처리
        }
    }
}
