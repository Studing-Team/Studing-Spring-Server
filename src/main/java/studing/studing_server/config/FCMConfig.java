package studing.studing_server.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FCMConfig {

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        // 파일 경로 수정
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("studing-fcm-firebase-adminsdk-ltwke-d4d183baf8.json").getInputStream());

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseMessaging.getInstance();
    }
}
