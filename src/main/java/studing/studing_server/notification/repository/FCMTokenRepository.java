package studing.studing_server.notification.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studing.studing_server.notification.entity.FCMToken;

import java.util.List;
import java.util.Optional;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByTokenAndEnabledTrue(String token);

    @Query("SELECT ft FROM FCMToken ft WHERE ft.member.id = :memberId AND ft.enabled = true")
    List<FCMToken> findAllValidTokensByMemberId(Long memberId);

    @Query("SELECT ft FROM FCMToken ft WHERE ft.member.memberUniversity = :universityName AND ft.enabled = true")
    List<FCMToken> findAllValidTokensByUniversity(String universityName);
}