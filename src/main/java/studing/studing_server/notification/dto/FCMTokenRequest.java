package studing.studing_server.notification.dto;

public record FCMTokenRequest(
        String fcmToken,
        Long memberId
) {}