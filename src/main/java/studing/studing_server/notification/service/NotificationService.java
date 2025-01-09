package studing.studing_server.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.repository.NoticeRepository;
import studing.studing_server.notification.entity.FCMToken;
import studing.studing_server.notification.entity.NoticeAlarm;
import studing.studing_server.notification.repository.FCMTokenRepository;
import studing.studing_server.notification.repository.NoticeAlarmRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {


    private final FCMTokenRepository fcmTokenRepository;
    private final FirebaseMessaging  firebaseMessaging;
    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeAlarmRepository noticeAlarmRepository;  // 추가


    @Transactional
    public void saveToken(Member member, String token, String platform) {
        // 기존 토큰이 있다면 비활성화
        fcmTokenRepository.findByTokenAndEnabledTrue(token)
                .ifPresent(FCMToken::disable);

        // 새 토큰 저장
        FCMToken fcmToken = new FCMToken(token, platform, member);
        fcmTokenRepository.save(fcmToken);
    }

    public void sendNotificationToMember(Long memberId, String title, String body, Map<String, String> data){



        String token = fcmTokenRepository.findValidTokenByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("No valid token found for member: " + memberId));

//        Notification notification = Notification.builder().setTitle(title).setBody(body).build();
//
//        // 메시지 구성
//        Message message = Message.builder()
//                .setToken(token) // 조회한 토큰 값을 사용
//                .setNotification(notification)
//                .putAllData(data)  // 추가 데이터 포함
//                .build();

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
            log.info("Message sent successfully for memberId: {} - Response: {}", memberId, response);
            System.out.println("Message sent successfully for memberId: " + memberId + " - Response: " + response);


        } catch (FirebaseMessagingException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();

        }




    }


    @Transactional
    public void setNoticeAlarm(String loginIdentifier, Long noticeId, LocalDateTime alarmTime) {
        // 현재 시간과 비교하여 과거 시간인지 확인
        if (alarmTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("알림 시간은 현재 시간 이후로 설정해야 합니다.");
        }

        // 회원 조회
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // 이미 설정된 알림이 있는지 확인
        if (noticeAlarmRepository.existsByNoticeIdAndMemberIdAndIsCompletedFalse(noticeId, member.getId())) {
            throw new IllegalStateException("이미 해당 공지사항에 대한 알림이 설정되어 있습니다.");
        }

        // FCM 토큰이 있는지 확인
        fcmTokenRepository.findValidTokenByMemberId(member.getId())
                .orElseThrow(() -> new IllegalStateException("알림을 받을 수 있는 기기가 등록되어 있지 않습니다."));

        // 알림 설정 저장
        NoticeAlarm noticeAlarm = NoticeAlarm.builder()
                .notice(notice)
                .member(member)
                .alarmTime(alarmTime)
                .build();

        noticeAlarmRepository.save(noticeAlarm);

        log.info("Notice alarm set - MemberId: {}, NoticeId: {}, AlarmTime: {}",
                member.getId(), noticeId, alarmTime);
    }


    @Transactional
    public void cancelNoticeAlarm(String loginIdentifier, Long noticeId) {
        // 회원 조회
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 해당 회원의 공지사항 알림 조회
        NoticeAlarm noticeAlarm = noticeAlarmRepository
                .findByNoticeIdAndMemberIdAndIsCompletedFalse(noticeId, member.getId())
                .orElseThrow(() -> new IllegalStateException("설정된 알림이 없습니다."));

        // 알림 취소 (삭제)
        noticeAlarmRepository.delete(noticeAlarm);

        log.info("Notice alarm cancelled - MemberId: {}, NoticeId: {}",
                member.getId(), noticeId);
    }


}
