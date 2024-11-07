package studing.studing_server.admain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.service.MemberVerificationService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberVerificationService memberVerificationService;

    @PostMapping("/members/{memberId}/verify")
    public ResponseEntity<SuccessStatusResponse<Void>> verifyMember(@PathVariable Long memberId) {
        memberVerificationService.verifyMember(memberId);

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.MEMBER_VERIFICATION_SUCCESS));
    }
}
