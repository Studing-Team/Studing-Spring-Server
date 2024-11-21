package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SavedNoticeResponse(
        Long id,
        String affiliation,
        String title,
        String content,
        String createdAt,
        boolean saveCheck
) {
    public static SavedNoticeResponse from(
            Long id,
            String affiliation,
            String title,
            String content,
            LocalDateTime createdAt,
            boolean saveCheck
    ) {
        return new SavedNoticeResponse(
                id,
                affiliation,
                title,
                content,
                createdAt.format(DateTimeFormatter.ISO_DATE),
                saveCheck
        );
    }
}