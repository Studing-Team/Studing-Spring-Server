package studing.studing_server.home.dto.notice;


import java.util.List;
import studing.studing_server.notices.dto.NoticeResponse;

public record RecentNoticesResponse(
        List<NoticeResponse> notices
) {}