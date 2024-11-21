package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NoticeResponse3(
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
        boolean likeCheck,
        String categorie
) {
    public static NoticeResponse3 from(
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
            boolean likeCheck,
            String categorie
    ) {
        return new NoticeResponse3(
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
                likeCheck,
                categorie
        );
    }
}