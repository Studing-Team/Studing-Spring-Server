package studing.studing_server.notification.service;

import com.google.firebase.messaging.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.notification.entity.FCMToken;
import studing.studing_server.notification.repository.FCMTokenRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final FCMTokenRepository fcmTokenRepository;

    @Transactional
    public void saveToken(Member member, String token) {
        // 기존 토큰이 있다면 비활성화
        fcmTokenRepository.findByTokenAndEnabledTrue(token)
                .ifPresent(FCMToken::disable);

        // 새 토큰 저장
        FCMToken fcmToken = new FCMToken(token, member);
        fcmTokenRepository.save(fcmToken);
    }

    public void sendNotificationToMember(Long memberId, String title, String body) {




        List<FCMToken> tokens = fcmTokenRepository.findAllValidTokensByMemberId(memberId);
        System.out.println("ddddddd");

        log.info("Found {} valid tokens for member {}", tokens.size(), memberId);
        tokens.forEach(token -> log.info("Token: {}", token.getToken()));



        sendNotification(tokens, title, body);





    }


    private void sendNotification(List<FCMToken> tokens, String title, String body) {

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for notification");
            return;
        }

        System.out.println("aaaaa");
        List<String> validTokens = tokens.stream()
                .map(FCMToken::getToken)
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());
        System.out.println("bbbb");
        if (validTokens.isEmpty()) {
            log.warn("No valid tokens found after filtering");
            return;
        }


        System.out.println("cccc");


        try {
            System.out.println("dddddd");
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(validTokens)
                    .build();
            System.out.println("eeee");

            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("FCM notification sent successfully: {}", response.getSuccessCount());

            System.out.println("ffff");
            System.out.println(response);
            System.out.println("gggg");
            handleFailedMessages(response, tokens);
            System.out.println("hhh");
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification", e);
            System.out.println("pppppp");
            // 로깅 및 예외 처리
        }
    }

    private void handleFailedMessages(BatchResponse response, List<FCMToken> tokens) {
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    FCMToken failedToken = tokens.get(i);
                    failedToken.disable();  // 실패한 토큰 비활성화
                    fcmTokenRepository.save(failedToken);
                }
            }
        }
    }
}
