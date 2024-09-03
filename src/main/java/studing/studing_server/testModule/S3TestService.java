package studing.studing_server.testModule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studing.studing_server.external.S3Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3TestService {

    private final S3Service s3Service;
    private final S3Repository s3Repository;

    private static final String TEST_S3_UPLOAD_FOLER = "tests3/";


    @Transactional
    public String create(S3CreateRequest s3CreateRequest) {

        try {
            S3Test s3Test = s3Repository.save(S3Test.create(s3CreateRequest.title(),
                    s3Service.uploadImage(TEST_S3_UPLOAD_FOLER, s3CreateRequest.image())));
            return s3Test.getId().toString();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
