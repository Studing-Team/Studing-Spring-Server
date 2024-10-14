package studing.studing_server.common.dto;

import studing.studing_server.common.exception.message.ErrorMessage;

public record ErrorResponse(
        int status,
        String message,
        Object data // 추가된 필드
) {
    public static ErrorResponse of(int status, String message, Object data) {
        return new ErrorResponse(status, message, data);
    }
    public static ErrorResponse of(ErrorMessage errorMessage) {
        return new ErrorResponse(errorMessage.getStatus(), errorMessage.getMessage(), null);
    }
}
