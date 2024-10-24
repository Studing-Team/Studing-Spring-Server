package studing.studing_server.universityData.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.universityData.entity.CollegeDepartment;
import studing.studing_server.universityData.entity.Department;

public interface CollegeDepartmentRepository extends JpaRepository<CollegeDepartment, Long> {

    // collegeDepartmentName과 universityName을 기반으로 CollegeDepartment 조회
    Optional<CollegeDepartment> findByCollegeDepartmentNameAndUniversity_UniversityName(String collegeDepartmentName, String universityName);
}
