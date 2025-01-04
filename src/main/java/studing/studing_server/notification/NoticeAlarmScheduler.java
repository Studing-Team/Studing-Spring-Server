package studing.studing_server.notification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.notification.entity.NoticeAlarm;
import studing.studing_server.notification.repository.NoticeAlarmRepository;
import studing.studing_server.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class NoticeAlarmScheduler {

    private final NoticeAlarmRepository noticeAlarmRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void processAlarms() {
        LocalDateTime now = LocalDateTime.now();

        // 현재 시간이 된 알림들을 조회
        List<NoticeAlarm> alarmsToProcess = noticeAlarmRepository.findByAlarmTimeBeforeAndIsCompletedFalse(now);

        for (NoticeAlarm alarm : alarmsToProcess) {
            try {
                // 추가 데이터 맵 생성
                Map<String, String> data = new HashMap<>();
                data.put("noticeId", alarm.getNotice().getId().toString());
                data.put("type", "NOTICE_ALARM");

                // 알림 발송
                notificationService.sendNotificationToMember(
                        alarm.getMember().getId(),
                        "공지사항 알림",
                        alarm.getNotice().getTitle(),  // 공지사항 제목을 알림 내용으로 사용
                        data
                );

                // 알림 처리 완료 표시
                alarm.markAsCompleted();
                noticeAlarmRepository.save(alarm);

                log.info("Successfully processed alarm - AlarmId: {}, MemberId: {}, NoticeId: {}",
                        alarm.getId(),
                        alarm.getMember().getId(),
                        alarm.getNotice().getId()
                );
            } catch (Exception e) {
                log.error("Failed to process alarm: {} - Error: {}", alarm.getId(), e.getMessage(), e);
            }
        }
    }
}