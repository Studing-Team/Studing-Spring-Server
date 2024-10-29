package studing.studing_server.member.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record NoticeCreateRequest(
        String title,
        String content,
        String tag,
        List<MultipartFile> noticeImages
) {
}