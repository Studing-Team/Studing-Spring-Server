package studing.studing_server.testModule;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class S3Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 200)
    private String title;
    private String imageUrl;

    @Builder
    public S3Test(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public static S3Test create(
            String title,
            String imageUrl
    ) {
        return new S3Test(title, imageUrl);
    }


}
