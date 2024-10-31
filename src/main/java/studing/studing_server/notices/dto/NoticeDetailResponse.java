package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.util.List;

// NoticeDetailResponse DTO 생성
public record NoticeDetailResponse(
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
) {}
