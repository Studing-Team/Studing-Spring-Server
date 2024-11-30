package studing.studing_server.member.service;

import com.amplitude.Amplitude;
import com.amplitude.Event;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import studing.studing_server.member.entity.Member;

@Service
@RequiredArgsConstructor
public class AmplitudeService {

    private final Amplitude amplitudeClient;

    public void trackSignUp(Member member) {
        try {
            // 유저 속성 설정
            JSONObject userProps = new JSONObject();
            userProps.put("id", member.getId());
            userProps.put("admission_number", member.getAdmissionNumber());
            userProps.put("name", member.getName());
            userProps.put("student_number", member.getStudentNumber());
            userProps.put("login_identifier", member.getLoginIdentifier());
            userProps.put("student_card_image", member.getStudentCardImage());
            userProps.put("university", member.getMemberUniversity());
            userProps.put("college_department", member.getMemberCollegeDepartment());
            userProps.put("department", member.getMemberDepartment());
            userProps.put("role", member.getRole());
            userProps.put("marketing_agreement", member.getMarketingAgreement());
            userProps.put("created_at", member.getCreatedAt());
            userProps.put("updated_at", member.getUpdatedAt());


            // 이벤트 생성
            Event event = new Event("sign_up", member.getLoginIdentifier());
            event.userProperties = userProps;

            // 필수 필드 설정
            event.deviceId = "device_" + member.getLoginIdentifier(); // deviceId는 필수입니다
            event.platform = "Web";  // 플랫폼 정보 추가

            // 이벤트 전송
            amplitudeClient.logEvent(event);

            // 이벤트가 확실히 전송되도록 강제로 플러시
            amplitudeClient.flushEvents();

        } catch (Exception e) {
            System.err.println("Failed to track sign up event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}