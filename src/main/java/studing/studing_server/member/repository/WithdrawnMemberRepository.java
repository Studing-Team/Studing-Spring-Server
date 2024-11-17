package studing.studing_server.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studing.studing_server.member.entity.WithdrawnMember;

@Repository
public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {
    boolean existsByLoginIdentifier(String loginIdentifier);
}
