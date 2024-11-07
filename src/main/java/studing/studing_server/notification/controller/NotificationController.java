package studing.studing_server.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @PostMapping("/token")
    public ResponseEntity<SuccessStatusResponse<Void>> registerToken(
            @RequestBody String fcmToken,
            Authentication authentication) {

        Member member = memberRepository.findByLoginIdentifier(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        notificationService.saveToken(member, fcmToken);

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.NOTIFICATION_TOKEN_REGISTERED));
    }
}
