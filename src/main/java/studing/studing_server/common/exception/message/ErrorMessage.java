package studing.studing_server.common.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    EMAIL_DUPLICATE(HttpStatus.CONFLICT.value(), "이미 사용중인 아이디 입니다."),
LOGIN_ID_NOT_FOUND(HttpStatus.UNAUTHORIZED.value(), "존재하지 않는 아이디입니다."),
    LOGIN_PASSWORD_INVALID(HttpStatus.UNAUTHORIZED.value(), "비밀번호가 일치하지 않습니다."),
    JWT_UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED.value(), "사용자의 로그인 검증을 실패했습니다."),
    ;
    private final int status;
    private final String message;
}