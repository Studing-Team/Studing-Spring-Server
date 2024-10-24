package studing.studing_server.universityData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.universityData.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentNameAndUniversity_UniversityName(String departmentName, String universityName);

    List<Department> findByUniversityId(Long universityId);
}