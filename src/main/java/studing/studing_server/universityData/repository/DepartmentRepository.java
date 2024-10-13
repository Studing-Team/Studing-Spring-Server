package studing.studing_server.universityData.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studing.studing_server.universityData.entity.Department;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByDepartmentName(String departmentName);
    List<Department> findByUniversityId(Long universityId);
}