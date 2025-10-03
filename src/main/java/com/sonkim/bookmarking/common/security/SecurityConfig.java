package com.sonkim.bookmarking.common.security;

import com.sonkim.bookmarking.common.aop.CachingRequestFilter;
import com.sonkim.bookmarking.auth.token.service.TokenService;
import com.sonkim.bookmarking.common.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;
    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final CachingRequestFilter cachingRequestFilter;
    private final JwtVerificationFilter jwtVerificationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 비밀번호 암호화 담당 컴포넌트
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);    // 인증 정보 포함 허용
            config.setAllowedOrigins(List.of(
                    "http://localhost:5173",
                    "https://localhost:5173",
                    "http://localhost:8080",
                    "https://marksphere.link"
            ));     // 허용할 도메인 설정
            config.setAllowedMethods(List.of("*"));     // 모든 HTTP 메서드 허용
            config.setAllowedHeaders(List.of("*"));     // 모든 헤더 허용

            return config;
        };
    }

    // 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authenticationManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));

        http
                // 불필요한 기능 비활성화
                .csrf(csrf -> csrf.disable())       // csrf 보안 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())        // http basic auth 기반으로 로그인 인증창이 뜨지 않게 설정
                .formLogin(formLogin -> formLogin.disable())        // formLogin 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))       // 세션 사용 안함
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));    // CORS 설정 적용

        http
                // 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 기본 페이지, 정적 소스들, Swagger 관련 경로, 'api/auth/**' 패턴의 URL은 모두 허용
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/fonts/**", "/error", "/favicon.ico",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(userAuthenticationEntryPoint));

        http
                // 멱등성 키 검증을 위해 캐싱 필터 추가
                .addFilterBefore(cachingRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // JWT 검증 필터 추가
                .addFilterBefore(jwtVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                // 로그인 필터 추가
                .addFilterAt(new LoginFilter(authenticationManager, jwtUtil, tokenService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
