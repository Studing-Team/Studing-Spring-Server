package studing.studing_server.testModule;

import org.springframework.web.multipart.MultipartFile;

public record S3CreateRequest(    String title,
                                  MultipartFile image) {

}
