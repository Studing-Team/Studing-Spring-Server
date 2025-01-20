package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record NoticeDetailResponse(
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
        boolean likeCheck,
        boolean isAuthor,
        String startTime,
        String endTime,
        boolean isFirstComeNotice
        ) {
    public static NoticeDetailResponse from(
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
            boolean likeCheck,
            boolean isAuthor,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean isFirstComeNotice

    ) {
        return new NoticeDetailResponse(
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
                likeCheck,
                isAuthor,
                startTime != null ? startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                endTime != null ? endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                isFirstComeNotice
        );
    }
}