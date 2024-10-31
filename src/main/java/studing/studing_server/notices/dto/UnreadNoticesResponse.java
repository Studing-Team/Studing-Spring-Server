package studing.studing_server.notices.dto;

import java.util.List;

public record UnreadNoticesResponse(
        List<UnreadNoticeResponse> notices
) {}