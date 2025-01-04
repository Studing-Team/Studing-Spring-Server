package studing.studing_server.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notification.entity.NoticeAlarm;

public interface NoticeAlarmRepository extends JpaRepository<NoticeAlarm, Long> {
    boolean existsByNoticeIdAndMemberIdAndIsCompletedFalse(Long noticeId, Long memberId);
    List<NoticeAlarm> findByAlarmTimeBeforeAndIsCompletedFalse(LocalDateTime time);
    Optional<NoticeAlarm> findByNoticeIdAndMemberIdAndIsCompletedFalse(Long noticeId, Long memberId);
}
