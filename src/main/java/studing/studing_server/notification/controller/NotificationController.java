package studing.studing_server.notification.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notification.dto.FCMTokenRequest;
import studing.studing_server.notification.dto.NoticeAlarmRequest;
import studing.studing_server.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;


    @PostMapping("/token")
    public ResponseEntity<SuccessStatusResponse<Void>> registerToken(
            @RequestBody FCMTokenRequest request) {

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        notificationService.saveToken(member, request.fcmToken());

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.NOTIFICATION_TOKEN_REGISTERED));
    }


    @PostMapping("/alarm/notice/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> setNoticeAlarm(
            HttpServletRequest request,
            @PathVariable Long noticeId,
            @RequestBody NoticeAlarmRequest alarmRequest) {

        // JWT 토큰에서 사용자 식별자 추출
        String loginIdentifier = jwtUtil.getLoginIdentifier(
                request.getHeader("Authorization").split(" ")[1]
        );

        // 알림 설정 서비스 호출
        notificationService.setNoticeAlarm(
                loginIdentifier,
                noticeId,
                LocalDateTime.of(
                        alarmRequest.year(),
                        alarmRequest.month(),
                        alarmRequest.day(),
                        alarmRequest.hour(),
                        alarmRequest.minute()
                )
        );

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_ALARM_SET_SUCCESS));
    }



    @DeleteMapping("/alarm/notice/{noticeId}")
    public ResponseEntity<SuccessStatusResponse<Void>> cancelNoticeAlarm(
            HttpServletRequest request,
            @PathVariable Long noticeId) {

        String loginIdentifier = jwtUtil.getLoginIdentifier(
                request.getHeader("Authorization").split(" ")[1]
        );

        notificationService.cancelNoticeAlarm(loginIdentifier, noticeId);

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.NOTICE_ALARM_CANCEL_SUCCESS));
    }





}
