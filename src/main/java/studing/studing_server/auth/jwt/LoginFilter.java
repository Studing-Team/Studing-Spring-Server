package studing.studing_server.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import studing.studing_server.common.dto.ErrorResponse;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.common.exception.message.ErrorMessage;
import studing.studing_server.member.dto.CustomMemberDetails;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import studing.studing_server.member.repository.MemberRepository;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;  // 추가


    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, MemberRepository memberRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        setFilterProcessesUrl("/api/v1/member/signin");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //클라이언트 요청에서 username, password 추출
        String loginIdentifier = obtainLoginIdentifier(request);
        String password = obtainPassword(request);


        // 1. 먼저 아이디 존재 여부 확인
        if (!memberRepository.existsByLoginIdentifier(loginIdentifier)) {
            unsuccessfulAuthentication(request, response, new AuthenticationException("ID_NOT_FOUND") {});
            return null;
        }


        try {
            //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginIdentifier, password, null);
            return authenticationManager.authenticate(authToken);  //token에 담은 검증을 위한 AuthenticationManager로 전달
        } catch (AuthenticationException e) {
            // 2. 비밀번호 불일치시
            unsuccessfulAuthentication(request, response, new AuthenticationException("PASSWORD_INVALID") {
            });
            return null;
        }
    }

    // loginId로 username을 대체하는 메서드
    protected String obtainLoginIdentifier(HttpServletRequest request) {
        return request.getParameter("loginIdentifier");
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        //UserDetailsS
        CustomMemberDetails customUserDetails = (CustomMemberDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 60*60*60*60*60*10L);

        // JWT 토큰을 응답 헤더에 추가하는 대신, 응답 바디의 data에 포함
        LoginResponseData loginData = new LoginResponseData(token);

        // SuccessStatusResponse를 통해 응답 바디 작성
        SuccessStatusResponse<LoginResponseData> successResponse = SuccessStatusResponse.of(SuccessMessage.SIGNIN_SUCCESS, loginData);

        // JSON 형식으로 응답 바디 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getWriter().write(convertObjectToJson(successResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        // 응답 상태 코드 설정 (401 Unauthorized)
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ErrorMessage errorMessage;

        if (failed.getMessage().equals("ID_NOT_FOUND")) {
            errorMessage = ErrorMessage.LOGIN_ID_NOT_FOUND;
        } else if (failed.getMessage().equals("PASSWORD_INVALID")) {
            errorMessage = ErrorMessage.LOGIN_PASSWORD_INVALID;
        } else {
            errorMessage = ErrorMessage.JWT_UNAUTHORIZED_EXCEPTION;
        }

        // 실패 응답 객체 생성
        ErrorResponse errorResponse = ErrorResponse.of(errorMessage.getStatus(), errorMessage.getMessage(), null);


        // 응답 바디에 JSON 형태로 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getWriter().write(convertObjectToJson(errorResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // JSON 변환을 위한 메서드 (Object를 JSON 문자열로 변환)
    private String convertObjectToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public static class LoginResponseData {
        private final String accessToken;

        public LoginResponseData(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }


    }
