package studing.studing_server.partner.dto;

import java.math.BigDecimal;

public record PartnerResponse(
        Long id,
        String partnerName,
        String partnerDescription,
        String partnerAddress,
        String category,
        String partnerContent,
        BigDecimal latitude,
        BigDecimal longitude,
        String partnerLogo  // 로고 이미지 URL
) {}
