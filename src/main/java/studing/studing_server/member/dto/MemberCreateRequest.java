package studing.studing_server.member.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberCreateRequest(
        Long admissionNumber,
        String name,
        String studentNumber,
        String loginIdentifier,
        String password,
        MultipartFile studentCardImage,
        String memberUniversity,
        String memberDepartment,
        Boolean marketingAgreement  // 마케팅 정보 수신 동의 필드 추가
) {}