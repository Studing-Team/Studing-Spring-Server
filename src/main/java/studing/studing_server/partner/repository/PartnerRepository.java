package studing.studing_server.partner.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studing.studing_server.partner.entity.Partner;

// PartnerRepository에 메서드 추가
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    @Query("SELECT p FROM Partner p WHERE p.university.id = :universityId AND (:category = '전체' OR p.category = :category)")
    List<Partner> findByUniversityIdAndCategory(
            @Param("universityId") Long universityId,
            @Param("category") String category);
}