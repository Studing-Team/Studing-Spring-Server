package studing.studing_server.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginIdentifier(String loginIdentifier);


}
