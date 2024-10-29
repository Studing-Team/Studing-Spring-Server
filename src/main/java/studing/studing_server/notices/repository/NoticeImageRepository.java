package studing.studing_server.notices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.notices.entity.NoticeImage;

public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {
}
