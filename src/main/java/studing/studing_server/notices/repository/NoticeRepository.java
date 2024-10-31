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


    @Query("SELECT COUNT(DISTINCT n) FROM Notice n " +
            "JOIN n.member m " +
            "JOIN n.noticeViews nv " +
            "WHERE m.memberUniversity = :universityName " +
            "AND m.memberCollegeDepartment = :categorie " +
            "AND n.createdAt BETWEEN :oneWeekAgo AND :now " +
            "AND nv.member.id = :memberId " +
            "AND nv.readAt = false")
    Long countUnreadNoticesInLastWeek(
            @Param("universityName") String universityName,
            @Param("categorie") String categorie,
            @Param("oneWeekAgo") LocalDateTime oneWeekAgo,
            @Param("now") LocalDateTime now,
            @Param("memberId") Long memberId
    );


    List<Notice> findTop5ByMember_MemberUniversityOrderByCreatedAtDesc(String memberUniversity);



}
