package studing.studing_server.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.io.IOException;

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

    @Transactional
    public void saveToken(Member member, String token) {
        // 기존 토큰이 있다면 비활성화
        fcmTokenRepository.findByTokenAndEnabledTrue(token)
                .ifPresent(FCMToken::disable);

        // 새 토큰 저장
        FCMToken fcmToken = new FCMToken(token, member);
        fcmTokenRepository.save(fcmToken);
    }

    public void sendNotificationToMember(Long memberId, String title, String body){

        String token = fcmTokenRepository.findValidTokenByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("No valid token found for member: " + memberId));



        // 메시지 구성
        Message message = Message.builder()
                .putData("title", title)
                .putData("content", body)
                .setToken(token) // 조회한 토큰 값을 사용
                .build();

        try {
            // 메시지 전송
            System.out.println("ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ " );

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Message sent successfully: " + response);

        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send message");
        }




    }








}
