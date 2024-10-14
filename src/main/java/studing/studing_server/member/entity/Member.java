package studing.studing_server.member.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studing.studing_server.common.BaseTimeEntity;
@Entity
@Getter
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


    @Builder
    public Member(Long admissionNumber, String name, String studentNumber, String loginIdentifier, String password, String studentCardImage,
                  String memberUniversity, String memberCollegeDepartment, String memberDepartment, String role) {
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
    }

}
