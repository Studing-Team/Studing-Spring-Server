package studing.studing_server.notices.dto;

import java.time.LocalDateTime;
import java.util.List;

// 응답 DTO 생성
public record UnreadNoticeResponse(
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
         boolean saveCheck,    // 추가
        boolean likeCheck    // 추가
) {}

