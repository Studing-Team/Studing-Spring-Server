package studing.studing_server.member.service;

import com.slack.api.Slack;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.ImageBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studing.studing_server.external.S3Service;
import studing.studing_server.member.dto.CheckLoginIdRequest;
import studing.studing_server.member.dto.MemberCreateRequest;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.universityData.entity.Department;
import studing.studing_server.universityData.repository.DepartmentRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final S3Service s3Service;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final Slack slack = Slack.getInstance();


    @Transactional
    public void signUp(MemberCreateRequest memberCreateRequest) {
        String imageUrl = uploadStudentCardImage(memberCreateRequest.studentCardImage());

        // departmentName과 memberUniversity 기반으로 department 조회
        Department department = departmentRepository
                .findByDepartmentNameAndUniversity_UniversityName(
                        memberCreateRequest.memberDepartment(),
                        memberCreateRequest.memberUniversity()
                )
                .orElseThrow(() -> new IllegalArgumentException("사용자가 입력한 학과 또는 대학이 잘못되었습니다."));

        // 조회한 department로부터 collegeDepartmentName 조회
        String memberCollegeDepartment = department.getCollegeDepartment().getCollegeDepartmentName();

        Member member = createMember(memberCreateRequest, imageUrl, memberCollegeDepartment);

        memberRepository.save(member);

        try {
            Payload payload = Payload.builder()
                    .blocks(Arrays.asList(
                            SectionBlock.builder()
                                    .text(MarkdownTextObject.builder()
                                            .text(String.format(
                                                    "*기본 정보*\n" +

                                                            "• *이름:* %s\n" +
                                                            "• *학번:* %s\n" +
                                                            "• *입학번호:* %d\n" +
                                                            "• *로그인 ID:* %s\n" +
                                                            "\n*소속 정보*\n" +
                                                            "• *대학교:* %s\n" +
                                                            "• *단과대학:* %s\n" +
                                                            "• *학과:* %s\n" +
                                                            "\n*부가 정보*\n" +
                                                            "• *현재 권한:* %s\n" +
                                                            "• *마케팅 .동의:* %s",

                                                    member.getName(),
                                                    member.getStudentNumber(),
                                                    member.getAdmissionNumber(),
                                                    member.getLoginIdentifier(),
                                                    member.getMemberUniversity(),
                                                    member.getMemberCollegeDepartment(),
                                                    member.getMemberDepartment(),
                                                    member.getRole(),
                                                            member.getMarketingAgreement() ? "동의" : "미동의"
                                            ))
                                            .build())
                                    .build(),
                            ImageBlock.builder()
                                    .imageUrl("https://studing-static-files.s3.ap-northeast-2.amazonaws.com/" + imageUrl)
                                    .altText("학생증 이미지")
                                    .build(),
                            ActionsBlock.builder()
                                    .elements(Arrays.asList(
                                            ButtonElement.builder()
                                                    .text(PlainTextObject.builder().text("승인").build())
                                                    .style("primary")
                                                    .value(member.getId().toString())
                                                    .actionId("approve_member")
                                                    .build(),
                                            ButtonElement.builder()
                                                    .text(PlainTextObject.builder().text("승인 불가").build())
                                                    .style("danger")
                                                    .value(member.getId().toString())
                                                    .actionId("reject_member")
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            WebhookResponse response = slack.send(webhookUrl, payload);
            if (response.getCode() != 200) {
                log.error("Failed to send Slack notification. Response: {}", response);
            }
        } catch (IOException e) {
            log.error("Failed to send Slack notification", e);
        }


        }





    private String uploadStudentCardImage(MultipartFile studentCardImage) {
        try {
            return s3Service.uploadImage("student-card-images/", studentCardImage);
        } catch (IOException e) {
            log.error("이미지 업로드에 실패했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다!", e);
        }
    }

    private Member createMember(MemberCreateRequest request, String imageUrl, String memberCollegeDepartment) {
        String hashedPassword = bCryptPasswordEncoder.encode(request.password());

        return Member.builder()
                .admissionNumber(request.admissionNumber())
                .name(request.name())
                .studentNumber(request.studentNumber())
                .loginIdentifier(request.loginIdentifier())
                .password(hashedPassword)
                .studentCardImage(imageUrl)
                .memberUniversity(request.memberUniversity())
                .memberDepartment(request.memberDepartment())
                .memberCollegeDepartment(memberCollegeDepartment)
                .role("ROLE_UNUSER")
                .marketingAgreement(request.marketingAgreement()) // 마케팅 동의 여부 추가
                .build();
    }







    public boolean isDuplicateEmail(CheckLoginIdRequest checkLoginIdRequest) {
        return memberRepository.existsByLoginIdentifier(checkLoginIdRequest.loginIdentifier());
    }














}
