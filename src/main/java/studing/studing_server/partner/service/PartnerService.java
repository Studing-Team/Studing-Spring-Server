package studing.studing_server.partner.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.member.entity.Member;
import studing.studing_server.member.repository.MemberRepository;
import studing.studing_server.partner.dto.PartnerResponse;
import studing.studing_server.partner.dto.PartnersResponse;
import studing.studing_server.partner.entity.Partner;
import studing.studing_server.partner.repository.PartnerRepository;
import studing.studing_server.universityData.entity.University;
import studing.studing_server.universityData.repository.UniversityDataRepository;

// Service 구현
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnerService {
    private static final String S3_PARTNER_LOGO_URL = "https://studing-static-files.s3.ap-northeast-2.amazonaws.com/partner/";
    private final MemberRepository memberRepository;
    private final UniversityDataRepository universityRepository;
    private final PartnerRepository partnerRepository;

    public PartnersResponse getPartnersByCategory(String loginIdentifier, String category) {
        // 1. 현재 사용자의 대학교 정보 조회
        Member currentMember = memberRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 2. 사용자의 대학교 ID 조회
        University university = universityRepository.findByUniversityName(currentMember.getMemberUniversity())
                .orElseThrow(() -> new IllegalArgumentException("해당 대학교를 찾을 수 없습니다."));

        // 3. 해당 대학교의 제휴업체 조회 (카테고리별)
        List<Partner> partners = partnerRepository.findByUniversityIdAndCategory(
                university.getId(),
                category
        );

        // 4. Response DTO로 변환
        List<PartnerResponse> partnerResponses = partners.stream()
                .map(partner -> new PartnerResponse(
                        partner.getId(),
                        partner.getPartnerName(),
                        partner.getPartnerDescription(),
                        partner.getPartnerAddress(),
                        partner.getCategory(),
                        partner.getPartnerContent(),
                        partner.getLatitude(),
                        partner.getLongitude(),
                        S3_PARTNER_LOGO_URL + partner.getPartnerImage()
                ))
                .collect(Collectors.toList());

        return new PartnersResponse(partnerResponses);
    }
}
