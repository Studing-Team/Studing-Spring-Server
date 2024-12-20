package studing.studing_server.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginIdentifier(String loginIdentifier);


    Optional<Member> findByLoginIdentifier(String loginIdentifier);
    List<Member> findByMemberUniversity(String universityName);
    boolean existsByMemberUniversityAndMemberDepartmentAndRole(
            String memberUniversity,
            String memberDepartment,
            String role
    );




    List<Member> findByMemberUniversityAndMemberCollegeDepartment(
            String memberUniversity,
            String memberCollegeDepartment
    );

    List<Member> findByMemberUniversityAndMemberDepartment(
            String memberUniversity,
            String memberDepartment
    );

}
