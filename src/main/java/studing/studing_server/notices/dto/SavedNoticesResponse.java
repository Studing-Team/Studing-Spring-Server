package studing.studing_server.notices.dto;

import java.util.List;

public record SavedNoticesResponse(
        List<SavedNoticeResponse> notices
) {}