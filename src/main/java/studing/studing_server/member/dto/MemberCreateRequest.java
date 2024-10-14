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
        String memberDepartment
) {}