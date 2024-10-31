package studing.studing_server.notices.dto;

import java.time.LocalDateTime;

// 응답 DTO들 생성
public record SavedNoticeResponse(
        Long id,
        String affiliation,
        String title,
        String content,
        LocalDateTime createdAt,
        boolean saveCheck
) {}

