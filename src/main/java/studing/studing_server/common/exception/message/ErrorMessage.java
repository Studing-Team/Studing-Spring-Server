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
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST.value(), "잘못된 카테고리입니다. '전체', '총학생회', '단과대', '학과' 중 하나를 입력해주세요."),
    INVALID_PARTNER_CATEGORY(HttpStatus.BAD_REQUEST.value(), "잘못된 카테고리입니다. '전체', ‘음식점’,’카페’,’운동’,문화’,주점’ 중 하나를 입력해주세요."),
    RESUBMIT_NOT_ALLOWED(HttpStatus.CONFLICT.value(), "승인 불가 상태의 회원만 학생증을 재제출할 수 있습니다."),
    NOT_FIRST_COME_NOTICE(HttpStatus.BAD_REQUEST.value(), "선착순 신청이 불가능한 공지사항입니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT.value(), "이미 신청한 공지사항입니다."),
    EXCEED_FIRST_COME_NUMBER(HttpStatus.CONFLICT.value(), "선착순 신청 인원이 초과되었습니다."),
    NOT_STARTED_FIRST_COME(HttpStatus.CONFLICT.value(), "아직 신청 시간이 되지 않았습니다."),
    ENDED_FIRST_COME(HttpStatus.CONFLICT.value(), "신청 기간이 종료되었습니다."),
    INVALID_ALARM_TIME(HttpStatus.BAD_REQUEST.value(), "알림 시간은 현재 시간 이후로 설정해야 합니다."),
    DUPLICATE_ALARM_SETTING(HttpStatus.CONFLICT.value(), "이미 해당 공지사항에 대한 알림이 설정되어 있습니다."),
    FCM_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "알림을 받을 수 있는 기기가 등록되어 있지 않습니다.");
    private final int status;
    private final String message;
}