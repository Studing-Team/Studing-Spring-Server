package studing.studing_server.admain.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.service.MemberVerificationService;
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberVerificationService memberVerificationService;

    @PostMapping()
    public ResponseEntity<Map<String, String>> handleSlackInteraction(@RequestParam("payload") String payloadStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadStr);

            String actionId = payload.path("actions").get(0).path("action_id").asText();
            String memberId = payload.path("actions").get(0).path("value").asText();

            // 버튼 액션에 따른 회원 검증
            if ("approve_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_USER");
            } else if ("reject_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_DENY");
            }

            Map<String, String> response = new HashMap<>();
            response.put("actionId", actionId);
            response.put("memberId", memberId);
            log.info("Slack Interaction Success - ActionId: {}, MemberId: {}", actionId, memberId);

            return ResponseEntity.ok(response);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Slack payload: {}", e.getMessage());
            log.error("Payload content: {}", payloadStr);
            log.error("Stack trace: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to parse payload: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error processing Slack interaction: {}", e.getMessage());
            log.error("Payload content: {}", payloadStr);
            log.error("Stack trace: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}




