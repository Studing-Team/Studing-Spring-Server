package studing.studing_server.member.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.service.MemberService;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;




    @PostMapping("/signup")
    public ResponseEntity<SuccessStatusResponse<Void>> signUp(@ModelAttribute MemberCreateRequest memberCreateRequest) {

        memberService.signUp(memberCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.SIGNUP_SUCCESS, null));
    }


}
