package studing.studing_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import studing.studing_server.auth.jwt.JWTFilter;
import studing.studing_server.auth.jwt.JWTUtil;
import studing.studing_server.auth.jwt.LoginFilter;
import studing.studing_server.member.repository.MemberRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          JWTUtil jwtUtil,
                          MemberRepository memberRepository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((auth) -> auth.disable());
        http
                .formLogin((auth) -> auth.disable());
        http
                .httpBasic((auth) -> auth.disable());
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(getPermitAllEndpoints()).permitAll() // 명시된 경로는 모든 사용자에게 허용
                        .requestMatchers("/admin").hasAnyRole("UNIVERSITY", "COLLEGE", "DEPARTMENT") // "/admin" 경로에 대한 권한 설정
                        .anyRequest().hasAnyRole("USER", "UNIVERSITY", "COLLEGE", "DEPARTMENT")); // 그 외 모든 경로는 권한 필요
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil,memberRepository), UsernamePasswordAuthenticationFilter.class);
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }


    private String[] getPermitAllEndpoints() {
        return new String[]{
                "/test/**",
                "/api/v1/admin/**",
                "/api/v1/member/signin",
                "/api/v1/universityData/university",
                "/api/v1/universityData/department",
                "/api/v1/member/signup",
                "/api/v1/member/checkid",
                "/",
        };
    }

}

