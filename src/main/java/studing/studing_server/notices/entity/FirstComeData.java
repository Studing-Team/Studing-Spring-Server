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
import studing.studing_server.common.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor
public class FirstComeData extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer orderNumber;      // 선착순 순서
    private String studentNumber;     // 학번

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Builder
    public FirstComeData(Integer orderNumber, String studentNumber, Notice notice) {
        this.orderNumber = orderNumber;
        this.studentNumber = studentNumber;
        this.notice = notice;
    }
}