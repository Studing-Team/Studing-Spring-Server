package studing.studing_server.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.notification.entity.FCMToken;
import studing.studing_server.notification.repository.FCMTokenRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        sendNotification(tokens, title, body);
    }

    public void sendNotificationToUniversity(String universityName, String title, String body) {
        List<FCMToken> tokens = fcmTokenRepository.findAllValidTokensByUniversity(universityName);
        sendNotification(tokens, title, body);
    }

    private void sendNotification(List<FCMToken> tokens, String title, String body) {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens.stream().map(FCMToken::getToken).toList())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            handleFailedMessages(response, tokens);
        } catch (FirebaseMessagingException e) {
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
