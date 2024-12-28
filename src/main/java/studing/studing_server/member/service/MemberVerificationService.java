package studing.studing_server.member.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final AmplitudeService amplitudeService;  // Amplitude 서비스 추가
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    private boolean isDevEnvironment() {
        return databaseUrl.endsWith("devstudingdb");
    }

    public void verifyMember(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        member.setRole(role);
        memberRepository.save(member);

        // Amplitude 추적이 필요한 역할 확인
        boolean needsAmplitudeTracking = Arrays.asList(
                "ROLE_USER", "ROLE_UNIVERSITY", "ROLE_COLLEGE", "ROLE_DEPARTMENT"
        ).contains(role);

        // devstudingdb가 아닐 때만 Amplitude 추적 실행
        if (needsAmplitudeTracking) {
            if (!isDevEnvironment()) {
                amplitudeService.trackSignUp(member);
            } else {
                log.info("Amplitude tracking skipped in dev environment for member: {}",
                        member.getId());
            }
        }

        // NotificationType 매핑
        NotificationType notificationType = switch (role) {
            case "ROLE_USER" -> NotificationType.USER;
            case "ROLE_UNIVERSITY" -> NotificationType.UNIVERSITY;
            case "ROLE_COLLEGE" -> NotificationType.COLLEGE;
            case "ROLE_DEPARTMENT" -> NotificationType.DEPARTMENT;
            case "ROLE_DENY" -> NotificationType.DENIED;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };

        sendVerificationNotification(member, notificationType);
    }


    private enum NotificationType {
        USER("학교 인증 완료", "%s님의 학교 인증이 완료되었습니다. 이제 Studing의 모든 서비스를 이용하실 수 있습니다."),
        UNIVERSITY("총학생회 권한 부여", "%s님의 학교 인증이 완료 되었으며 총학생회 권한이 부여되었습니다. 이제 총학생회 공지사항을 작성하실 수 있습니다."),
        COLLEGE("단과대학 학생회 권한 부여", "%s님의 학교 인증이 완료 되었으며 단과대 학생회 권한이 부여되었습니다. 이제 단과대학 공지사항을 작성하실 수 있습니다."),
        DEPARTMENT("학과 학생회 권한 부여", "%s님의 학교 인증이 완료 되었으며 학과 학생회 권한이 부여되었습니다. 이제 학과 공지사항을 작성하실 수 있습니다."),
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
        // 알림 데이터 맵 생성
        Map<String, String> data = new HashMap<>();
        data.put("type", "VERIFICATION");  // 알림 타입을 구분하기 위한 값


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
                    body,
                    data
            );
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}
