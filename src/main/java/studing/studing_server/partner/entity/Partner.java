package studing.studing_server.partner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import studing.studing_server.universityData.entity.University;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Partner {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private  String partnerName;
    private  String category;
    private  String partnerContent;
    private  String partnerDescription;
    private  String partnerAddress;
    private String partnerImage;

    @Column(precision = 10, scale = 8)  // DECIMAL(10,8)
    private BigDecimal latitude;        // 위도

    @Column(precision = 11, scale = 8)  // DECIMAL(11,8)
    private BigDecimal longitude;       // 경도

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;





}
