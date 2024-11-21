package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record UnreadNoticeResponse(
        Long id,
        String title,
        String content,
        Long likeCount,
        Long saveCount,
        Long readCount,
        String createdAt,
        String affilitionName,
        String logoImage,
        String tag,
        List<String> images,
        boolean saveCheck,
        boolean likeCheck
) {
    public static UnreadNoticeResponse from(
            Long id,
            String title,
            String content,
            Long likeCount,
            Long saveCount,
            Long readCount,
            LocalDateTime createdAt,
            String affilitionName,
            String logoImage,
            String tag,
            List<String> images,
            boolean saveCheck,
            boolean likeCheck
    ) {
        return new UnreadNoticeResponse(
                id,
                title,
                content,
                likeCount,
                saveCount,
                readCount,
                createdAt.format(DateTimeFormatter.ISO_DATE),
                affilitionName,
                logoImage,
                tag,
                images,
                saveCheck,
                likeCheck
        );
    }
}