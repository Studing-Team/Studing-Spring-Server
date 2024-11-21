package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        String writerInfo,
        Long noticeLike,
        Long viewCount,
        Long saveCount,
        String image,
        String createdAt,
        boolean saveCheck,
        boolean likeCheck
) {
    public static NoticeResponse from(
            Long id,
            String title,
            String content,
            String writerInfo,
            Long noticeLike,
            Long viewCount,
            Long saveCount,
            String image,
            LocalDateTime createdAt,
            boolean saveCheck,
            boolean likeCheck
    ) {
        return new NoticeResponse(
                id,
                title,
                content,
                writerInfo,
                noticeLike,
                viewCount,
                saveCount,
                image,
                createdAt.format(DateTimeFormatter.ISO_DATE),
                saveCheck,
                likeCheck
        );
    }
}