package studing.studing_server.member.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.common.exception.message.BusinessException;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.member.dto.CheckLoginIdRequest;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.dto.SignUpResponse;
import studing.studing_server.member.service.MemberService;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


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




}
