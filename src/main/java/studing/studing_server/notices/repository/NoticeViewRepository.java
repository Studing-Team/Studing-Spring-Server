package studing.studing_server.notices.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.member.entity.Member;
import studing.studing_server.notices.entity.Notice;
import studing.studing_server.notices.entity.NoticeView;

public interface NoticeViewRepository extends JpaRepository<NoticeView, Long> {
    Optional<NoticeView> findByNoticeAndMember(Notice notice, Member member);
    boolean existsByMemberIdAndNoticeId(Long memberId, Long noticeId);
    Optional<NoticeView> findByMemberIdAndNoticeId(Long memberId, Long noticeId);
}
