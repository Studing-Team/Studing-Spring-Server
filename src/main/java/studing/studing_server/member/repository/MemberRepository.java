package studing.studing_server.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginIdentifier(String loginIdentifier);

    //loginIdentifier 받아 DB 테이블에서 회원을 조회하는 메소드 작성
    Optional<Member> findByLoginIdentifier(String loginIdentifier);
}
