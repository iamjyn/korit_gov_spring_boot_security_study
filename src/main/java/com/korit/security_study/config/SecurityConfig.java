package com.korit.security_study.config;

import com.korit.security_study.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
    * BCryptPasswordEncoder
    * 비밀번호를 안전하게 암호화하고, 검증하는 역할
    * 단방향 해시, 복호화 불가능
    * */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    * CORS(Cross-Origin Resource Sharing)
    * 브라우저 보안상 다른 도메인의 리소스 요청을 제한하는 정책
    * 기본적으로 브라우저는 같은 출처(Same-Origin)만 허용
    * */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 요청을 보내는 쪽의 도메인(사이트 주소)을 허용
        corsConfiguration.addAllowedOriginPattern(CorsConfiguration.ALL);
        // 요청을 보내는 쪽에서 Request, Response의 Header 정보에 대한 제약을 모두 허용
        corsConfiguration.addExposedHeader(CorsConfiguration.ALL);
        // 요청을 보내는 쪽의 메소드에 대해서 모두 허용
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);

        // 요청 URL에 대한 CORS 설정을 적용하기 위해 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 URL 주소에 대해서 위에서 설정한 CORS 정책을 적용
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()); // 위에서 만든 CORS 설정을 security에 적용
        http.csrf(csrf -> csrf.disable());
        /*
        * CSRF란?
        * => 사용자가 의도하지 않은 요청을 공격자가 유도해서 서버에 전달하도록 하는 공격
        * JWT 방식 또는 무상태(STATELESS) 인증 방식을 사용하기 때문에
        * 세션이 없고, 쿠키도 안 쓰고, 토큰 기반의 인증 방식을 사용해서 CSRF 공격 자체가 성립되지 않는다.
        * */

        // 서버 사이드 렌더링 로그인 방식 비활성화
        http.formLogin(formLogin -> formLogin.disable());
        // HTTP 프로토콜 로그인 방식 비활성화
        http.httpBasic(httpBasic -> httpBasic.disable());
        // 서버 사이드 렌더링 로그아웃 방식 비활성화
        http.logout(logout -> logout.disable());
        // 세션의 무상태 방식 사용
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // jwt 필터 적용
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 특정 요청 URL에 대한 권한 설정
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/auth/signup", "/auth/signin").permitAll();
            auth.anyRequest().authenticated();
        });

        // OAuth2 설정

        return http.build();
    }
}
