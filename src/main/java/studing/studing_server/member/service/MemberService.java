package studing.studing_server.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.repository.DepartmentRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final S3Service s3Service;




    @Transactional
    public void signUp(MemberCreateRequest memberCreateRequest) {
        String imageUrl = uploadStudentCardImage(memberCreateRequest.studentCardImage());



        // departmentName 기반으로 department 조회
        Department department = departmentRepository.findByDepartmentName(memberCreateRequest.memberDepartment())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 입력한 학과가 잘못 되었습니다."));

        // 조회한 department로부터 collegeDepartmentName 조회
        String memberCollegeDepartment = department.getCollegeDepartment().getCollegeDepartmentName();

        Member member = createMember(memberCreateRequest, imageUrl, memberCollegeDepartment);


        memberRepository.save(member);




    }

    private String uploadStudentCardImage(MultipartFile studentCardImage) {
        try {
            return s3Service.uploadImage("student-card-images/", studentCardImage);
        } catch (IOException e) {
            log.error("이미지 업로드에 실패했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다!", e);
        }
    }

    private Member createMember(MemberCreateRequest request, String imageUrl, String memberCollegeDepartment) {
        //String hashedPassword = passwordEncoder.encode(request.password());
        String hashedPassword="dd";


        return Member.builder()
                .admissionNumber(request.admissionNumber())
                .name(request.name())
                .studentNumber(request.studentNumber())
                .loginIdentifier(request.loginIdentifier())
                .password(hashedPassword)
                .studentCardImage(imageUrl)
                .memberUniversity(request.memberUniversity())
                .memberDepartment(request.memberDepartment())
                .memberCollegeDepartment(memberCollegeDepartment)
                .role("ROLE_UNUSER")
                .build();
    }




}
