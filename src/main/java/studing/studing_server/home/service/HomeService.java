package studing.studing_server.home.service;

import org.springframework.stereotype.Service;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.universityData.entity.CollegeDepartment;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.CollegeDepartmentRepository;
import studing.studing_server.universityData.repository.DepartmentRepository;
import studing.studing_server.universityData.repository.UniversityDataRepository;

@Service
public class HomeService {

    private final MemberRepository memberRepository;
    private final UniversityDataRepository universityDataRepository;
    private final CollegeDepartmentRepository collegeDepartmentRepository;
    private final DepartmentRepository departmentRepository;

    public HomeService(MemberRepository memberRepository, UniversityDataRepository universityDataRepository,
                       CollegeDepartmentRepository collegeDepartmentRepository, DepartmentRepository departmentRepository) {
        this.memberRepository = memberRepository;
        this.universityDataRepository = universityDataRepository;
        this.collegeDepartmentRepository = collegeDepartmentRepository;
        this.departmentRepository = departmentRepository;
    }

    public LogoResponse getLogoWithName(String loginIdentifier) {
        // Member 테이블에서 사용자의 memberUniversity, memberCollegeDepartment, memberDepartment 정보 조회
        Member member = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // University에서 universityName에 해당하는 정보 조회
        University university = universityDataRepository.findByUniversityName(member.getMemberUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 대학을 찾을 수 없습니다."));

        // CollegeDepartment에서 collegeDepartmentName에 해당하는 정보 조회
        CollegeDepartment collegeDepartment = collegeDepartmentRepository
                .findByCollegeDepartmentNameAndUniversity_UniversityName(member.getMemberCollegeDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 단과대를 찾을 수 없습니다."));

        // Department에서 memberDepartment에 해당하는 정보 조회
        Department department = departmentRepository.findByDepartmentNameAndUniversity_UniversityName(member.getMemberDepartment(), university.getUniversityName())
                .orElseThrow(() -> new IllegalArgumentException("해당 학과를 찾을 수 없습니다."));


        return new LogoResponse(
                university.getUniversityLogoImage(),
                "총학생회",
                collegeDepartment.getCollegeDepartmentLogoImage(),
                collegeDepartment.getCollegeDepartmentName(),
                department.getDepartmentImage(),
                department.getDepartmentName()
        );
    }
}
