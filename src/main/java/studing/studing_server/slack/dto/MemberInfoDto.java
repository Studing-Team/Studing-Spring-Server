package studing.studing_server.slack.dto;

import lombok.Getter;
import studing.studing_server.member.entity.Member;

@Getter
public class MemberInfoDto {
    private final String name;
    private final String studentNumber;
    private final Long admissionNumber;
    private final String loginIdentifier;
    private final String university;
    private final String collegeDepartment;
    private final String department;
    private final String role;
    private final boolean marketingAgreement;

    private MemberInfoDto(Member member) {
        this.name = member.getName();
        this.studentNumber = member.getStudentNumber();
        this.admissionNumber = member.getAdmissionNumber();
        this.loginIdentifier = member.getLoginIdentifier();
        this.university = member.getMemberUniversity();
        this.collegeDepartment = member.getMemberCollegeDepartment();
        this.department = member.getMemberDepartment();
        this.role = member.getRole();
        this.marketingAgreement = member.getMarketingAgreement();
    }

    public static MemberInfoDto from(Member member) {
        return new MemberInfoDto(member);
    }

    public String toMarkdownText() {
        return String.format(
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
                        "• *마케팅 동의:* %s",
                name, studentNumber, admissionNumber, loginIdentifier,
                university, collegeDepartment, department,
                role, marketingAgreement ? "동의" : "미동의"
        );
    }
}
