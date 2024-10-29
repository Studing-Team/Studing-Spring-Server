package studing.studing_server.notices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import studing.studing_server.notices.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
