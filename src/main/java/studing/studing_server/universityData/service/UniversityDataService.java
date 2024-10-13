package studing.studing_server.universityData.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.DepartmentRepository;
import studing.studing_server.universityData.repository.UniversityDataRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityDataService {

    private final UniversityDataRepository universityDataRepository;
    private final DepartmentRepository departmentRepository;

    public List<String> getAllUniversityNames() {
        List<University> universities = universityDataRepository.findAll();
        return universities.stream()
                .map(University::getUniversityName)
                .collect(Collectors.toList());
    }


    public List<String> getDepartmentNamesByUniversity(String universityName) {
        University university = universityDataRepository.findByUniversityName(universityName)
                .orElseThrow(() -> new IllegalArgumentException("University not found: " + universityName));

        List<Department> departments = departmentRepository.findByUniversityId(university.getId());
        return departments.stream()
                .map(Department::getDepartmentName)
                .collect(Collectors.toList());
    }





}

