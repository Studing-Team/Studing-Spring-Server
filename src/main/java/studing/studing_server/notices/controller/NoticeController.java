package studing.studing_server.notices.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.member.dto.NoticeCreateRequest;
import studing.studing_server.notices.service.NoticeService;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {
    private  final NoticeService noticeService;



    @PostMapping("/create")
    public ResponseEntity<SuccessStatusResponse<Void>> createPost(@ModelAttribute NoticeCreateRequest noticeCreateRequest) {
        System.out.println("ddsfsdsafassdafsafsdfsadfsdfsdf");


        noticeService.createPost(noticeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessStatusResponse.of(SuccessMessage.CREATE_NOTICE_SUCCESS, null));
    }







}
