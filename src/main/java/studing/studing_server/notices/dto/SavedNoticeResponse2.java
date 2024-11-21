package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SavedNoticeResponse2(
        Long id,
        String title,
        String createdAt,
        String image,
        boolean saveCheck
) {
    public static SavedNoticeResponse2 of(
            Long id,
            String title,
            LocalDateTime createdAt,
            String image,
            boolean saveCheck
    ) {
        return new SavedNoticeResponse2(
                id,
                title,
                createdAt.format(DateTimeFormatter.ISO_DATE),
                image,
                saveCheck
        );
    }
}