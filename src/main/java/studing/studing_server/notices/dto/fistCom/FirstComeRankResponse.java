package studing.studing_server.notices.dto.fistCom;

import java.util.List;

public record FirstComeRankResponse(
        List<FirstComeRankItem> rankings,
        Integer myRanking     // 현재 사용자의 순번 (미신청시 null)
) {}