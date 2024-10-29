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
import studing.studing_server.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor
public class NoticeView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;


    private boolean readAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;



    @Builder
    public NoticeView(Notice notice, Member member, boolean readAt) {
        this.notice = notice;
        this.member = member;
        this.readAt = readAt;
    }

    public void setReadAt(boolean readAt) {
        this.readAt = readAt;
    }

}

