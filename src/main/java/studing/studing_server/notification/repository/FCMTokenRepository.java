package studing.studing_server.notification.repository;


import java.awt.print.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studing.studing_server.notification.entity.FCMToken;

import java.util.List;
import java.util.Optional;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByTokenAndEnabledTrue(String token);


    @Query("SELECT ft.token FROM FCMToken ft WHERE ft.member.id = :memberId AND ft.enabled = true ORDER BY ft.id DESC")
    Optional<String> findValidTokenByMemberId(@Param("memberId") Long memberId);







}