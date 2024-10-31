package studing.studing_server.partner.dto;

import java.util.List;

public record PartnersResponse(
        List<PartnerResponse> partners
) {}
