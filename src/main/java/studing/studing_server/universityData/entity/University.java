package studing.studing_server.universityData.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String universityName;

    private String universityLogoImage;

    private String  universityNickName;

    @OneToMany(mappedBy = "university")
    private List<CollegeDepartment> collegeDepartments;


}
