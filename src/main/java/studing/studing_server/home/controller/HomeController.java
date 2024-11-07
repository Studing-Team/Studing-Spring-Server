package studing.studing_server.home.controller;



import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.home.dto.LogoResponse;
import studing.studing_server.home.dto.MemberDataResponse;
import studing.studing_server.home.dto.UnreadCategoryResponse;
import studing.studing_server.home.dto.UnreadNoticeCountRequest;
import studing.studing_server.home.dto.UnreadNoticeCountResponse;
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


    @GetMapping("/mydata")
    public ResponseEntity<SuccessStatusResponse<MemberDataResponse>> getMyData(HttpServletRequest request) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);

        // HomeService에서 Member 데이터를 조회
        MemberDataResponse memberDataResponse = homeService.getMyData(loginIdentifier);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.DATA_FETCH_SUCCESS, memberDataResponse));
    }

    @GetMapping("/unread-categories")
    public ResponseEntity<SuccessStatusResponse<UnreadCategoryResponse>> getUnreadCategories(
            HttpServletRequest request) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        UnreadCategoryResponse unreadCategories = homeService.getUnreadCategories(loginIdentifier);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(
                        SuccessMessage.UNREAD_CATEGORIES_FETCH_SUCCESS,
                        unreadCategories));
    }


    @PostMapping("/unread-notice-count")
    public ResponseEntity<SuccessStatusResponse<UnreadNoticeCountResponse>> getUnreadNoticeCount(
            HttpServletRequest request,
            @RequestBody UnreadNoticeCountRequest unreadRequest) {

        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        UnreadNoticeCountResponse response = homeService.getUnreadNoticeCount(loginIdentifier, unreadRequest.categorie());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_COUNT_FETCH_SUCCESS, response));
    }










}
