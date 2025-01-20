package studing.studing_server.notices.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.FirstComeData;

public interface FirstComeDataRepository extends JpaRepository<FirstComeData, Long> {
    List<FirstComeData> findByNoticeIdOrderByOrderNumberAsc(Long noticeId);
    Optional<FirstComeData> findTopByNoticeIdOrderByOrderNumberDesc(Long noticeId);
    long countByNoticeId(Long noticeId);
    boolean existsByNoticeIdAndStudentNumber(Long noticeId, String studentNumber);

}