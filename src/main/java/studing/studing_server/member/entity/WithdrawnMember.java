package studing.studing_server.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studing.studing_server.common.BaseTimeEntity;

// WithdrawnMember.java
@Entity
@Getter
@NoArgsConstructor
public class WithdrawnMember extends BaseTimeEntity {
    @Id
    private Long id;  // 기존 member의 id를 그대로 사용

    private Long admissionNumber;
    private String name;
    private String studentNumber;
    private String loginIdentifier;
    private String memberUniversity;
    private String memberCollegeDepartment;
    private String memberDepartment;
    private String role;
    private LocalDateTime withdrawnAt;
    private String withdrawalReason;  // 탈퇴 사유 (선택적)

    @Builder
    public WithdrawnMember(Member member, String withdrawalReason) {
        this.id = member.getId();
        this.admissionNumber = member.getAdmissionNumber();
        this.name = member.getName();
        this.studentNumber = member.getStudentNumber();
        this.loginIdentifier = member.getLoginIdentifier();
        this.memberUniversity = member.getMemberUniversity();
        this.memberCollegeDepartment = member.getMemberCollegeDepartment();
        this.memberDepartment = member.getMemberDepartment();
        this.role = member.getRole();
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = withdrawalReason;
    }
}
