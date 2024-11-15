package studing.studing_server.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class FCMConfig {
    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("Starting Firebase Messaging initialization...");
        try {
            String resourcePath = "studing-fcm-firebase-adminsdk-ltwke-d4d183baf8.json";
            log.info("Loading credentials from: {}", resourcePath);

            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.error("Credentials file not found: {}", resourcePath);
                throw new IOException("Credentials file not found");
            }

            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(resource.getInputStream());
            log.info("Credentials loaded successfully");

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();
            log.info("Firebase options built successfully");

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions);
                log.info("Firebase Application initialized: {}", app.getName());
            } else {
                log.info("Firebase Application already initialized");
            }

            FirebaseMessaging instance = FirebaseMessaging.getInstance();
            log.info("Firebase Messaging instance created successfully");
            return instance;
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: ", e);
            throw e;
        }
    }
}