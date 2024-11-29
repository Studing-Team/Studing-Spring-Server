package studing.studing_server.member.dto;

import org.springframework.web.multipart.MultipartFile;

public record MemberResubmitRequest(
        String studentNumber,
        String name,
        MultipartFile studentCardImage
) {}
