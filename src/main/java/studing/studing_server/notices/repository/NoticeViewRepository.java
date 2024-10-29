package studing.studing_server.notices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.NoticeView;

public interface NoticeViewRepository extends JpaRepository<NoticeView, Long> {

}

