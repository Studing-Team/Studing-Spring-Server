package studing.studing_server.universityData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.universityData.entity.University;

import java.util.Optional;

public interface UniversityDataRepository extends JpaRepository<University, Long> {
    Optional<University> findByUniversityName(String universityName);
}
