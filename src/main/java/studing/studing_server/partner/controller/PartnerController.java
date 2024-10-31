package studing.studing_server.partner.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.partner.dto.PartnerCategoryRequest;
import studing.studing_server.partner.dto.PartnersResponse;
import studing.studing_server.partner.service.PartnerService;

// Controller 추가
@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;
    private final JWTUtil jwtUtil;

    @PostMapping
    public ResponseEntity<SuccessStatusResponse<PartnersResponse>> getPartnersByCategory(
            HttpServletRequest request,
            @RequestBody PartnerCategoryRequest categoryRequest) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        PartnersResponse response = partnerService.getPartnersByCategory(loginIdentifier, categoryRequest.categorie());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.PARTNERS_FETCH_SUCCESS, response));
    }
}