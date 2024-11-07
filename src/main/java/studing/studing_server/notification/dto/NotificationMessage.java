package studing.studing_server.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationMessage {
    private String title;
    private String body;
    private String image;      // 선택적 이미지 URL
    private String clickAction;// 알림 클릭시 이동할 화면
    private Object data;       // 추가 데이터

    public static NotificationMessage memberVerification(String memberName) {
        return NotificationMessage.builder()
                .title("학교 인증 완료")
                .body(memberName + "님의 학교 인증이 완료되었습니다.")
                .clickAction("MAIN_SCREEN")
                .build();
    }
}
