package studing.studing_server.notices.dto;

import java.util.List;

public record SavedNoticesResponse2(
        List<SavedNoticeResponse2> notices
) {}