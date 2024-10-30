package studing.studing_server.home.dto;

import java.util.List;

public record UnreadCategoryResponse(
        List<String> categories
) {}