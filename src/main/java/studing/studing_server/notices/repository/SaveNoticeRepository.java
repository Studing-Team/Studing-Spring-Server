package studing.studing_server.notices.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.SaveNotice;

public interface SaveNoticeRepository extends JpaRepository<SaveNotice, Long> {
    boolean existsByMemberIdAndNoticeId(Long memberId, Long noticeId);
    Optional<SaveNotice> findByMemberIdAndNoticeId(Long memberId, Long noticeId);
}
