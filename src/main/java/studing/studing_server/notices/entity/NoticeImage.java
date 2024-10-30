package studing.studing_server.notices.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@NoArgsConstructor
@Entity
public class NoticeImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String  noticeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice;

    @Builder
    public NoticeImage(String noticeImage, Notice notice) {
        this.noticeImage = noticeImage;
        this.notice = notice;
    }
    public void setNotice(Notice notice) {
        this.notice = notice;
    }

}
