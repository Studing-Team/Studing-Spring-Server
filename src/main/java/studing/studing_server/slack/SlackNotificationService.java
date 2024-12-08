package studing.studing_server.slack;


import com.slack.api.Slack;
import com.slack.api.model.block.ActionsBlock;
import com.slack.api.model.block.ImageBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import studing.studing_server.member.entity.Member;
import studing.studing_server.slack.dto.MemberInfoDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {


    private static final String S3_BASE_URL = "https://studing-static-files.s3.ap-northeast-2.amazonaws.com/";

    @Value("${slack.webhook.url}")
    private String webhookUrl;



    private final Slack slack = Slack.getInstance();



    public void sendMemberVerificationRequest(Member member, String imageUrl, Long memberCount) {
        try {
            Payload payload = createMemberVerificationPayload(member, imageUrl, false, memberCount);
            sendSlackNotification(payload);
        } catch (IOException e) {
            log.error("Failed to send Slack notification", e);
        }
    }



    public void sendMemberResubmissionRequest(Member member, String imageUrl) {
        try {
            Payload payload = createMemberVerificationPayload(member, imageUrl, true,0L);
            sendSlackNotification(payload);
        } catch (IOException e) {
            log.error("Failed to send Slack notification for resubmission", e);
        }
    }


    private Payload createMemberVerificationPayload(Member member, String imageUrl, boolean isResubmission, Long memberCount) {
        return Payload.builder()
                .blocks(Arrays.asList(
                        createHeaderSection(isResubmission, memberCount),
                        createMemberInfoSection(member),
                        createImageBlock(imageUrl),
                        createActionButtons(member.getId())
                ))
                .build();
    }

    private SectionBlock createHeaderSection(boolean isResubmission, Long memberCount) {
        String headerText = isResubmission
                ? "*:rotating_light: 학생증 재제출 요청*"
                : String.format("*:new: 신규 회원가입 요청*\n*%d번째 사용자가 회원가입 하였습니다!*", memberCount);

        return SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(headerText)
                        .build())
                .build();
    }



    private SectionBlock createMemberInfoSection(Member member) {
        MemberInfoDto info = MemberInfoDto.from(member);
        return SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(info.toMarkdownText())
                        .build())
                .build();
    }

    private ImageBlock createImageBlock(String imageUrl) {
        return ImageBlock.builder()
                .imageUrl(S3_BASE_URL + imageUrl)
                .altText("학생증 이미지")
                .build();
    }

    private ActionsBlock createActionButtons(Long memberId) {
        return ActionsBlock.builder()
                .elements(Arrays.asList(
                        createButton("승인", "primary", "approve_member", memberId),
                        createButton("승인 불가", "danger", "reject_member", memberId),
                        createButton("총학생회 권한 부여", "primary", "university_member", memberId),
                        createButton("단과대 학생회 권한 부여", "primary", "college_member", memberId),
                        createButton("학과 학생회 권한 부여", "primary", "department_member", memberId)

                ))
                .build();
    }

    private ButtonElement createButton(String text, String style, String actionId, Long memberId) {
        return ButtonElement.builder()
                .text(PlainTextObject.builder().text(text).build())
                .style(style)
                .value(memberId.toString())
                .actionId(actionId)
                .build();
    }

    private void sendSlackNotification(Payload payload) throws IOException {
        WebhookResponse response = slack.send(webhookUrl, payload);
        if (response.getCode() != 200) {
            log.error("Failed to send Slack notification. Response: {}", response);
        }
    }
}
