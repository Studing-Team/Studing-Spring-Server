package studing.studing_server.notices.dto;

import java.time.LocalDateTime;

// 응답 DTO들 생성
public record SavedNoticeResponse2(
        Long id,
        String title,
        LocalDateTime createdAt,
        boolean saveCheck
) {}
