package studing.studing_server.member.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.common.exception.message.BusinessException;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.member.dto.ChangePasswordRequest;
import studing.studing_server.member.dto.CheckLoginIdRequest;
import studing.studing_server.member.dto.FindPasswordRequest;
import studing.studing_server.member.dto.FindPasswordResponse;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.dto.MemberResubmitRequest;
import studing.studing_server.member.dto.SignUpResponse;
import studing.studing_server.member.service.MemberService;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;



    @PostMapping("/checkid")
    public ResponseEntity<SuccessStatusResponse<Void>> checkEmail(@RequestBody CheckLoginIdRequest checkEmailRequest) {
        boolean isDuplicate = memberService.isDuplicateEmail(checkEmailRequest);
        if (isDuplicate) {
            throw new BusinessException(ErrorMessage.EMAIL_DUPLICATE);
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(SuccessStatusResponse.of(SuccessMessage.EMAIL_AVAILABLE));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<SuccessStatusResponse<SignUpResponse>> signUp(@ModelAttribute MemberCreateRequest memberCreateRequest) {
        SignUpResponse response = memberService.signUp(memberCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.SIGNUP_SUCCESS, response));
    }


    @DeleteMapping("/withdraw")
    public ResponseEntity<SuccessStatusResponse<Void>> withdrawMember(
            HttpServletRequest request
          ) {

        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);

        memberService.withdrawMember(loginIdentifier);

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.MEMBER_WITHDRAWAL_SUCCESS));
    }

    @PostMapping("/resubmit")
    public ResponseEntity<SuccessStatusResponse<Void>> resubmitStudentCard(
            HttpServletRequest request,
            @ModelAttribute MemberResubmitRequest resubmitRequest) {


        String loginIdentifier = jwtUtil.getLoginIdentifier(request.getHeader("Authorization").split(" ")[1]);
        memberService.resubmitStudentCard(loginIdentifier, resubmitRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.STUDENT_CARD_RESUBMIT_SUCCESS));
    }



    @PostMapping("/find-password")
    public ResponseEntity<SuccessStatusResponse<FindPasswordResponse>> findPassword(
            @RequestBody FindPasswordRequest request) {
        FindPasswordResponse response = memberService.findPassword(request.loginIdentifier());
        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.PASSWORD_RESET_SUCCESS, response));
    }
    @PostMapping("/change-password")
    public ResponseEntity<SuccessStatusResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest) {

        memberService.changePassword(
                changePasswordRequest.loginIdentifier(),
                changePasswordRequest.currentPassword(),
                changePasswordRequest.newPassword()
        );

        return ResponseEntity.ok()
                .body(SuccessStatusResponse.of(SuccessMessage.PASSWORD_CHANGE_SUCCESS));
    }


}
