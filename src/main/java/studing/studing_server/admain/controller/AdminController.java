package studing.studing_server.admain.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.member.service.MemberVerificationService;

import java.util.Base64;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberVerificationService memberVerificationService;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${amplitude.api-key}")
    private String amplitudeApiKey;

    @Value("${amplitude.api-secret}")
    private String amplitudeApiSecret;

    private static final String AMPLITUDE_API_URL = "https://amplitude.com/api/2/deletions/users";


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



    @PostMapping("/delete-amplitude-data/{amplitudeId}")
    public ResponseEntity<?> deleteAmplitudeData(@PathVariable String amplitudeId) {
        try {
            HttpHeaders headers = createAmplitudeHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            // amplitude_ids를 Long 배열로 전송
            requestBody.put("amplitude_ids", List.of(Long.parseLong(amplitudeId)));
            requestBody.put("requester", "Admin Deletion Request");
            requestBody.put("ignore_invalid_id", "True");     // boolean이 아닌 String으로 변경
            requestBody.put("delete_from_org", "True");       // boolean이 아닌 String으로 변경


            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> amplitudeResponse = restTemplate.exchange(
                    "https://amplitude.com/api/2/deletions/users",  // 정확한 엔드포인트 URL
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (amplitudeResponse.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully requested Amplitude data deletion for amplitude ID: {}", amplitudeId);
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "데이터 삭제 요청이 성공적으로 처리되었습니다. 삭제는 30일 이내에 완료됩니다.",
                                "amplitudeId", amplitudeId,
                                "response", amplitudeResponse.getBody()
                        ));
            } else {
                log.error("Failed to delete Amplitude data for amplitude ID: {}", amplitudeId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "데이터 삭제 요청에 실패했습니다."));
            }

        } catch (Exception e) {
            log.error("Error deleting Amplitude data for amplitude ID: {}", amplitudeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "데이터 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    @GetMapping("/amplitude-deletion-status")
    public ResponseEntity<?> checkDeletionStatus(
            @RequestParam String startDay,  // YYYY-MM-DD 형식
            @RequestParam String endDay) {  // YYYY-MM-DD 형식

        try {
            HttpHeaders headers = createAmplitudeHeaders();
            headers.set("Accept", "application/json");  // Accept 헤더 추가
            HttpEntity<String> request = new HttpEntity<>(headers);

            String url = "https://amplitude.com/api/2/deletions/users" +
                    "?start_day=" + startDay +
                    "&end_day=" + endDay;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "삭제 상태 확인 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    private HttpHeaders createAmplitudeHeaders() {
        String credentials = amplitudeApiKey + ":" + amplitudeApiSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());


        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Basic " + encodedCredentials);
        return headers;
    }








}




