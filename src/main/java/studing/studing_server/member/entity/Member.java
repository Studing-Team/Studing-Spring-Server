package studing.studing_server.member.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


}
