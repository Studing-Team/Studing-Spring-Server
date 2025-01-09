package studing.studing_server.notification.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studing.studing_server.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor
public class FCMToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String platform; // "iOS" 또는 "ANDROID"

    private boolean enabled = true;

    public FCMToken(String token, String platform, Member member) {
        this.token = token;
        this.platform = platform;
        this.member = member;
    }

    public void disable() {
        this.enabled = false;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}