package studing.studing_server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class FirebaseInitialization {

    @PostConstruct
    public void initialize() {
        try {
            // 클래스패스를 통해 JSON 파일 로드
            InputStream serviceAccount =
                    this.getClass().getClassLoader().getResourceAsStream("studing-a8837-firebase-adminsdk-wl5fu-e2415a1c7f.json");

            // 파일이 없으면 예외 처리
            if (serviceAccount == null) {
                throw new IllegalStateException("Firebase Admin SDK JSON 파일을 찾을 수 없습니다.");
            }

            // Firebase 초기화 옵션 설정
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
