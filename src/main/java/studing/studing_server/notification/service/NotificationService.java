package studing.studing_server.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.notification.entity.FCMToken;
import studing.studing_server.notification.repository.FCMTokenRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {


    private final FCMTokenRepository fcmTokenRepository;
    private final FirebaseMessaging  firebaseMessaging;

    @Transactional
    public void saveToken(Member member, String token) {
        // 기존 토큰이 있다면 비활성화
        fcmTokenRepository.findByTokenAndEnabledTrue(token)
                .ifPresent(FCMToken::disable);

        // 새 토큰 저장
        FCMToken fcmToken = new FCMToken(token, member);
        fcmTokenRepository.save(fcmToken);
    }

    public void sendNotificationToMember(Long memberId, String title, String body, Map<String, String> data){

        String token = fcmTokenRepository.findValidTokenByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("No valid token found for member: " + memberId));

        // 기존 data에 title과 body도 포함시킴
        data.put("title", title);
        data.put("body", body);

        // notification 필드 없이 data만 포함하여 메시지 구성
        Message message = Message.builder()
                .setToken(token)
                .putAllData(data)  // 모든 데이터를 data 필드로 전송
                .build();

        try {
            // 프론트엔드가 받게 될 페이로드 출력
            System.out.println("\n======= FCM Payload for Frontend =======");

            // Data 필드
            System.out.println("data: {");
            data.forEach((key, value) ->
                    System.out.println("    " + key + ": " + value));
            System.out.println("}");

            System.out.println("token: " + token);
            System.out.println("=====================================\n");


            String response= firebaseMessaging.send(message);
            log.info("Message sent successfully: {}", response);
            System.out.println("Message sent successfully: " + response);

        } catch (FirebaseMessagingException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();

        }




    }








}
