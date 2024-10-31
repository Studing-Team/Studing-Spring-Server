package studing.studing_server.notices.dto;


import java.util.List;

public record RecentNoticesResponse(
        List<NoticeResponse> notices
) {}