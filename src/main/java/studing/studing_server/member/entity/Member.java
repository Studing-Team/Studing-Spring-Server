package studing.studing_server.member.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studing.studing_server.common.BaseTimeEntity;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeLike;
import studing.studing_server.notices.entity.NoticeView;
import studing.studing_server.notices.entity.SaveNotice;
import studing.studing_server.notification.entity.FCMToken;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private  Long admissionNumber;

    private String name;

    private String studentNumber;

    private String loginIdentifier;

    private String password;

    private String studentCardImage;

    private String memberUniversity;

    private String memberCollegeDepartment;

    private String memberDepartment;

    private String  role;

    @Column(nullable = false) // null 불가능하도록 설정
    private Boolean marketingAgreement = false; // 기본값 설정


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notice> notices;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeLike> noticeLikes;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaveNotice> saveNotices;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeView> noticeView;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FCMToken> fcmTokens;


    @Builder
    public Member(Long admissionNumber, String name, String studentNumber,
                  String loginIdentifier, String password, String studentCardImage,
                  String memberUniversity, String memberCollegeDepartment,
                  String memberDepartment, String role, Boolean marketingAgreement) {
        this.admissionNumber = admissionNumber;
        this.name = name;
        this.studentNumber = studentNumber;
        this.loginIdentifier = loginIdentifier;
        this.password = password;
        this.studentCardImage = studentCardImage;
        this.memberUniversity = memberUniversity;
        this.memberCollegeDepartment = memberCollegeDepartment;
        this.memberDepartment = memberDepartment;
        this.role = role;
        this.marketingAgreement = marketingAgreement;
    }

}
