package studing.studing_server.home.controller;



import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.home.service.HomeService;


@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final JWTUtil jwtUtil;


    @GetMapping("/logo")
    public ResponseEntity<SuccessStatusResponse<LogoResponse>> getLogoWithName(HttpServletRequest request) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        LogoResponse logoResponse = homeService.getLogoWithName(loginIdentifier);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.LOGO_FETCH_SUCCESS, logoResponse));
    }


}
