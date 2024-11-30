package studing.studing_server.config;

import com.amplitude.Amplitude;


import com.amplitude.AmplitudeLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AmplitudeConfig {

    @Value("${amplitude.api-key}")
    private String amplitudeApiKey;

q

    @Bean
    public Amplitude amplitudeClient() {
        // 새로운 인스턴스 생성
        Amplitude client = Amplitude.getInstance();

        // API 키로 초기화
        client.init(amplitudeApiKey);

        // 추가 설정
        client.setEventUploadThreshold(1);  // 이벤트를 즉시 전송
        client.setEventUploadPeriodMillis(30000); // 30초 주기로 전송

        // 로그 모드 설정 (디버깅을 위해)
        client.setLogMode(com.amplitude.AmplitudeLog.LogMode.DEBUG);

        return client;
    }
}
