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
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.member.service.MemberVerificationService;
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberVerificationService memberVerificationService;
    private final MemberRepository memberRepository;
    @PostMapping()
    public ResponseEntity<Map<String, String>> handleSlackInteraction(@RequestParam("payload") String payloadStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(payloadStr);

            String actionId = payload.path("actions").get(0).path("action_id").asText();
            String memberId = payload.path("actions").get(0).path("value").asText();


            // memberId로 현재 회원 정보 조회
            Member member = memberRepository.findById(Long.parseLong(memberId))
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

            // 현재 회원의 role이 ROLE_UNUSER가 아닌 경우 처리 중단
            if (!"ROLE_UNUSER".equals(member.getRole())) {
                log.warn("Member verification failed - Member: {} already has role: {}",
                        member.getId(),
                        member.getRole());

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Member already verified",
                                "message", String.format("회원(ID: %d)은 이미 %s 권한을 가지고 있습니다.",
                                        member.getId(),
                                        translateRole(member.getRole()))
                        ));
            }


            // 버튼 액션에 따른 회원 검증
            if ("approve_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_USER");
            } else if ("reject_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_DENY");
            } else if ("university_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_UNIVERSITY");
            }else if ("college_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_COLLEGE");
            }else if ("department_member".equals(actionId)) {
                memberVerificationService.verifyMember(Long.parseLong(memberId), "ROLE_DEPARTMENT");
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



    private String translateRole(String role) {
        return switch (role) {
            case "ROLE_USER" -> "일반 사용자";
            case "ROLE_UNIVERSITY" -> "총학생회";
            case "ROLE_COLLEGE" -> "단과대학 학생회";
            case "ROLE_DEPARTMENT" -> "학과 학생회";
            case "ROLE_DENY" -> "거부됨";
            case "ROLE_UNUSER" -> "미승인";
            default -> role;
        };
    }


}




