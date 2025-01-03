package studing.studing_server.notices.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.home.dto.UnreadNoticeCountRequest;
import studing.studing_server.member.dto.NoticeCreateRequest;
import studing.studing_server.notices.dto.NoticeDetailResponse;
import studing.studing_server.home.dto.notice.RecentNoticesResponse;
import studing.studing_server.notices.dto.RecentNoticesResponse2;
import studing.studing_server.notices.dto.SavedNoticesResponse2;
import studing.studing_server.notices.dto.UnreadNoticesResponse;
import studing.studing_server.notices.service.NoticeService;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {
    private  final NoticeService noticeService;
    private final JWTUtil jwtUtil;


    @PostMapping("/create")
    public ResponseEntity<SuccessStatusResponse<Void>> createPost(@ModelAttribute NoticeCreateRequest noticeCreateRequest) {
        noticeService.createPost(noticeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.CREATE_NOTICE_SUCCESS, null));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessStatusResponse<RecentNoticesResponse>> getAllNotices(
            HttpServletRequest request) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        RecentNoticesResponse response = noticeService.getAllNotices(loginIdentifier);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.ALL_NOTICES_FETCH_SUCCESS, response));
    }

    @PostMapping("/all-category")
    public ResponseEntity<SuccessStatusResponse<RecentNoticesResponse2>> getAllCategoryNotices(
            HttpServletRequest request,
            @RequestBody UnreadNoticeCountRequest categorieRequest) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        RecentNoticesResponse2 response = noticeService.getAllCategoryNotices(loginIdentifier, categorieRequest.categorie());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.ALL_CATEGORY_NOTICES_FETCH_SUCCESS, response));
    }



    @PostMapping("/save/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> saveNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.saveNotice(loginIdentifier, noticeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_SAVE_SUCCESS));
    }

    @DeleteMapping("/save/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> cancelSaveNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.cancelSaveNotice(loginIdentifier, noticeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_SAVE_CANCEL_SUCCESS));
    }

    @PostMapping("/like/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> likeNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.likeNotice(loginIdentifier, noticeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_LIKE_SUCCESS));
    }


    @DeleteMapping("/like/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> cancelLikeNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.cancelLikeNotice(loginIdentifier, noticeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_LIKE_CANCEL_SUCCESS));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<NoticeDetailResponse>> getNoticeDetail(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        NoticeDetailResponse response = noticeService.getNoticeDetail(loginIdentifier, noticeId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_DETAIL_FETCH_SUCCESS, response));
    }



    @PostMapping("/save-category")
    public ResponseEntity<SuccessStatusResponse<SavedNoticesResponse2>> getSavedNoticesByCategory(
            HttpServletRequest request,
            @RequestBody UnreadNoticeCountRequest categorieRequest) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        SavedNoticesResponse2 response = noticeService.getSavedNoticesByCategory(loginIdentifier, categorieRequest.categorie());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.SAVED_NOTICES_BY_CATEGORY_SUCCESS, response));
    }

    @PostMapping("/view-check/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> checkNoticeView(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        boolean isNewView = noticeService.checkNoticeView(loginIdentifier, noticeId);

        if (isNewView) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_VIEW_CHECK_SUCCESS));
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_ALREADY_VIEWED));
        }
    }


    @PostMapping("/unread/all")
    public ResponseEntity<SuccessStatusResponse<UnreadNoticesResponse>> getAllUnreadNotices(
            HttpServletRequest request,
            @RequestBody UnreadNoticeCountRequest categorieRequest) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        UnreadNoticesResponse response = noticeService.getAllUnreadNotices(loginIdentifier, categorieRequest.categorie());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.UNREAD_NOTICES_FETCH_SUCCESS, response));
    }



    @DeleteMapping("/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> deleteNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.deleteNotice(loginIdentifier, noticeId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_DELETE_SUCCESS));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> updateNotice(
            HttpServletRequest request,
            @PathVariable Long noticeId,
            @ModelAttribute NoticeCreateRequest noticeUpdateRequest) {
        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        noticeService.updateNotice(loginIdentifier, noticeId, noticeUpdateRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_UPDATE_SUCCESS));
    }



}
