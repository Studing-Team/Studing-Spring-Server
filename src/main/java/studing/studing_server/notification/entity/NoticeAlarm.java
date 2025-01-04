package studing.studing_server.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studing.studing_server.member.entity.Member;
import studing.studing_server.notices.entity.Notice;

@Entity
@Getter
@NoArgsConstructor
public class NoticeAlarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime alarmTime;

    private boolean isCompleted = false;  // 알림 발송 완료 여부

    @Builder
    public NoticeAlarm(Notice notice, Member member, LocalDateTime alarmTime) {
        this.notice = notice;
        this.member = member;
        this.alarmTime = alarmTime;
    }

    public void markAsCompleted() {
        this.isCompleted = true;
    }
}