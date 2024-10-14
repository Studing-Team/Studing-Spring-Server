package studing.studing_server.universityData.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departmentName;

    private String departmentImage;
    private String departmentNickName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_department_id")
    private CollegeDepartment collegeDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;



}