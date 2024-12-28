package studing.studing_server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessMessage {

    UNIVERSITY_GET_SUCCESS(HttpStatus.OK.value(),"대학교이름 조회에 성공하였습니다."),
    MEMBER_VERIFICATION_SUCCESS(HttpStatus.OK.value(),"사용자 권한 수정 후 알림을 보냈습니다."),
    NOTIFICATION_TOKEN_REGISTERED(HttpStatus.OK.value(),"FCM 토큰 저장에 성공했습니다.."),
    DEPARTMENT_GET_SUCCESS(HttpStatus.OK.value(),"대학교이름을 통해 학과 조회에 성공하였습니다."),
    EMAIL_AVAILABLE(HttpStatus.OK.value(),"사용가능한 아이디입니다."),
    SIGNUP_SUCCESS(HttpStatus.CREATED.value(),"회원가입에 성공하였습니다."),
    SIGNIN_SUCCESS(HttpStatus.OK.value(),"로그인에 성공하였습니다."),
    LOGO_FETCH_SUCCESS(HttpStatus.OK.value(),"로고와 이름 데이터 조회에 성공하였습니다."),
    DATA_FETCH_SUCCESS(HttpStatus.OK.value(),"회원 데이터 조회에 성공하였습니다."),
    MEMBER_WITHDRAWAL_SUCCESS(HttpStatus.OK.value(), "회원 탈퇴가 완료되었습니다."),
    NOTICE_ALREADY_VIEWED(HttpStatus.OK.value(), "이미 조회한 공지사항입니다."),
    NOTICE_VIEW_CHECK_SUCCESS(HttpStatus.CREATED.value(), "공지사항 조회 체크에 성공하였습니다."),
    // SuccessMessage.java에 추가
    PASSWORD_RESET_SUCCESS(HttpStatus.OK.value(), "임시 비밀번호가 발급되었습니다."),
    // SuccessMessage에 추가
    NOTICE_SAVE_CANCEL_SUCCESS(HttpStatus.OK.value(), "공지사항 저장 취소에 성공하였습니다."),
    // SuccessMessage에 추가
    NOTICE_SAVE_SUCCESS(HttpStatus.CREATED.value(), "공지사항 저장에 성공하였습니다."),
    // SuccessMessage.java에 추가
    STUDENT_CARD_RESUBMIT_SUCCESS(HttpStatus.OK.value(), "학생증 재제출이 완료되었습니다."),
    // SuccessMessage에 추가
    NOTICE_LIKE_SUCCESS(HttpStatus.CREATED.value(), "공지사항 좋아요에 성공하였습니다."),
    // SuccessMessage에 추가
    NOTICE_LIKE_CANCEL_SUCCESS(HttpStatus.OK.value(), "공지사항 좋아요 취소에 성공하였습니다."),
    // SuccessMessage에 추가
    NOTICE_DETAIL_FETCH_SUCCESS(HttpStatus.OK.value(), "개별 공지 조회에 성공하였습니다."),
    // SuccessMessage에 추가
    SAVED_NOTICES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "저장한 공지들 조회에 성공하였습니다."),
    // SuccessMessage에 추가
    PARTNERS_FETCH_SUCCESS(HttpStatus.CREATED.value(), "카테고리별 제휴업체 데이터 조회에 성공하였습니다."),
    // SuccessMessage에 추가
    UNREAD_NOTICES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "카테고리별 놓친 공지 조회에 성공하였습니다."),
    // SuccessMessage에 추가
    SAVED_NOTICES_BY_CATEGORY_SUCCESS(HttpStatus.CREATED.value(), "카테고리별 저장한 공지들 조회에 성공하였습니다."),
//    JWT_LOGIN_SUCCESS(HttpStatus.UNAUTHORIZED.value(), "가입된 계정이 없습니다 아이디와 비밀번호를 다시 확인해주세요."),
RECENT_NOTICES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "최근 공지사항 조회에 성공하였습니다."),
    ALL_NOTICES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "모든 공지사항 조회에 성공하였습니다."),
    ALL_CATEGORY_NOTICES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "카테고리별 모든 공지사항 조회에 성공하였습니다."),
    CREATE_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지작성에 성공하였습니다."),
    NOTICE_COUNT_FETCH_SUCCESS(HttpStatus.CREATED.value(), "읽지않은 카테고리별 공지 개수 조회에 성공하였습니다."),
    UNREAD_CATEGORIES_FETCH_SUCCESS(HttpStatus.CREATED.value(), "읽지않은 카테고리 명 데이터 조회에 성공하였습니다."),
//    LIKE_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 좋아요에 성공하였습니다."),
//    LIKE_CANCLE_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 좋아요 취소에 성공하였습니다."),
//    SAVE_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 저장에 성공하였습니다."),
//    SAVE_CANCLE_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 저장 취소에 성공하였습니다."),
//    SAVE_ALL_NOTICE_SUCCESS(HttpStatus.OK.value(),"공지 저장 리스트 조회에 성공하였습니다."),
//    VIEW_COUNT_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 조회 체크 후 조회수 증가 성공하였습니다."),
//    VIEW_CHECK_NOTICE_SUCCESS(HttpStatus.CREATED.value(),"공지 조회 체크에 성공하였습니다."),
//    GET_QUCIK_HEAD_SUCCESS(HttpStatus.OK.value(),"퀵스캔 스토리 헤드 데이터 조회에 성공하였습니다."),
//    GET_ALL_NOTICE_SUCCESS(HttpStatus.OK.value(),"전체 공지 조회에 성공하였습니다."),
//    GET_ALL_UNIVERSITY_NOTICE_SUCCESS(HttpStatus.OK.value(),"총학생회 공지 조회에 성공하였습니다."),
//    GET_ALL_COLLEGE_NOTICE_SUCCESS(HttpStatus.OK.value(),"단과대학 학생회 공지 조회에 성공하였습니다."),
//    GET_ALL_DEPARTMENT_NOTICE_SUCCESS(HttpStatus.OK.value(),"학과 학생회 공지 조회에 성공하였습니다."),
//    GET_QUICK_NOTICE_SUCCESS(HttpStatus.OK.value(),"퀵스캔 공지 조회에 성공하였습니다."),
//    GET_Detail_NOTICE_SUCCESS(HttpStatus.OK.value(),"개별 공지 조회에 성공하였습니다."),
//    GET_MYPAGE_SUCCESS(HttpStatus.OK.value(),"마이페이지 조회에 성공하였습니다.")
    ;
    private final int status;
    private final String message;
}