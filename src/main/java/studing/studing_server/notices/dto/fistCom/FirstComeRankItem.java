package studing.studing_server.notices.dto.fistCom;

import java.time.format.DateTimeFormatter;
import studing.studing_server.notices.entity.FirstComeData;

public record FirstComeRankItem(
        Integer orderNumber,             // 순번
        String applyDateTime,            // 신청 시간
        String maskedStudentNumber       // 마스킹된 학번
) {
    public static FirstComeRankItem from(FirstComeData data) {
        return new FirstComeRankItem(
                data.getOrderNumber(),
                data.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS")),
                maskStudentNumber(data.getStudentNumber())
        );
    }

    // 학번 마스킹 처리 (예: ****0890)
    private static String maskStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < 4) {
            return studentNumber;
        }
        return "*".repeat(studentNumber.length() - 4) +
                studentNumber.substring(studentNumber.length() - 4);
    }
}