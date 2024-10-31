package studing.studing_server.notices.dto;


import java.time.LocalDateTime;

public record NoticeResponse2(
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
) {}
