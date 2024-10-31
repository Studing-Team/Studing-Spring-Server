package studing.studing_server.notices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.SaveNotice;

public interface SaveNoticeRepository extends JpaRepository<SaveNotice, Long> {
    boolean existsByMemberIdAndNoticeId(Long memberId, Long noticeId);
}
