package studing.studing_server.notices.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studing.studing_server.notices.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 특정 대학교의 일주일 이내 공지사항 조회
    @Query("SELECT n FROM Notice n JOIN n.member m " +
            "WHERE m.memberUniversity = :universityName " +
            "AND n.createdAt >= :oneWeekAgo")
    List<Notice> findAllByMemberUniversityAndCreatedAtAfter(
            @Param("universityName") String universityName,
            @Param("oneWeekAgo") LocalDateTime oneWeekAgo);
}
