package studing.studing_server.notices.repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studing.studing_server.notices.entity.SaveNotice;

public interface SaveNoticeRepository extends JpaRepository<SaveNotice, Long> {
    boolean existsByMemberIdAndNoticeId(Long memberId, Long noticeId);
    Optional<SaveNotice> findByMemberIdAndNoticeId(Long memberId, Long noticeId);




    List<SaveNotice> findTop5ByMemberIdOrderByNoticeCreatedAtDesc(Long memberId);
    List<SaveNotice> findByMemberIdOrderByNoticeCreatedAtDesc(Long memberId);


}
