package studing.studing_server.config;

import com.amplitude.Amplitude;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AmplitudeConfig {

    private final String apiKey;

    public AmplitudeConfig(@Value("${amplitude.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean
    public Amplitude amplitudeClient() {
        Amplitude client = Amplitude.getInstance();
        client.init(apiKey);
        return client;
    }
}
