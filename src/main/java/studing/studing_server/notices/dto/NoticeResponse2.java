package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NoticeResponse2(
        Long id,
        String title,
        String content,
        String tag,
        Long noticeLike,
        Long viewCount,
        Long saveCount,
        String image,
        String createdAt,
        boolean saveCheck,
        boolean likeCheck
) {
    public static NoticeResponse2 from(
            Long id,
            String title,
            String content,
            String tag,
            Long noticeLike,
            Long viewCount,
            Long saveCount,
            String image,
            LocalDateTime createdAt,
            boolean saveCheck,
            boolean likeCheck
    ) {
        return new NoticeResponse2(
                id,
                title,
                content,
                tag,
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