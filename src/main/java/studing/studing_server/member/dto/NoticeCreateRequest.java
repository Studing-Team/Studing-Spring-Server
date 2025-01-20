package studing.studing_server.member.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public record NoticeCreateRequest(
        String title,
        String content,
        String tag,
        List<MultipartFile> noticeImages,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")  // 날짜 형식 지정
        LocalDateTime startTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")  // 날짜 형식 지정
        LocalDateTime endTime,
        Integer firstComeNumber
) {
}