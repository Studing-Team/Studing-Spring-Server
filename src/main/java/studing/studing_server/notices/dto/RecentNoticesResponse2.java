package studing.studing_server.notices.dto;

import java.util.List;

public record RecentNoticesResponse2(
        List<NoticeResponse2> notices
) {}