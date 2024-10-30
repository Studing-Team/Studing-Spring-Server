package studing.studing_server.notices.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studing.studing_server.notices.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // member(작성자)의 memberUniversity가 주어진 universityName과 일치하는 공지사항들 조회
    @Query("SELECT n FROM Notice n JOIN n.member m WHERE m.memberUniversity = :universityName")
    List<Notice> findAllByMemberUniversity(@Param("universityName") String universityName);
}
