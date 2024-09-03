package studing.studing_server.testModule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final S3TestService s3TestService;


    @GetMapping("/test")
    public String test() {
        return " 테스트 API 입니다!";
    }

    @GetMapping("/health")
    public String healthTest() {
        return " Health Check 성공!";
    }

    @PostMapping("/tests3")
    public ResponseEntity createS3(
            @ModelAttribute S3CreateRequest s3CreateRequest
    ) {
        return ResponseEntity.created(URI.create(s3TestService.create(
                s3CreateRequest))).build();
    }




}
