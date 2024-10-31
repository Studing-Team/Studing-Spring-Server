package studing.studing_server.notices.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.NoticeLike;


public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {
    boolean existsByMemberIdAndNoticeId(Long memberId, Long noticeId);
}