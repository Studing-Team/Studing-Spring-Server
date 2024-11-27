package studing.studing_server.member.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberResubmitRequest(
        Long admissionNumber,
        String name,
        MultipartFile studentCardImage
) {}
