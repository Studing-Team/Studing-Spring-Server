package studing.studing_server.notification.dto;

public record NoticeAlarmRequest(
        int year,
        int month,
        int day,
        int hour,
        int minute
) {}