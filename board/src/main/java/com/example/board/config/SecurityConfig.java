package com.example.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.board.jwt.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API + JWT면 CSRF 비활성화
            .csrf(csrf -> csrf.disable())
            // 세션 없이 동작
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 인가 규칙
            .authorizeHttpRequests(auth -> auth
                // 로그인/회원가입 & 중복체크만 공개
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/check-username", "/auth/check-nickname").permitAll()
                // 문서 공개
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 회원탈퇴는 인증 필수
                .requestMatchers(HttpMethod.DELETE, "/auth/withdraw").authenticated()
                // 나머지 보호 API
                .requestMatchers("/api/boards/**").authenticated()
                .requestMatchers("/scraps/**", "/users/**").authenticated()
                .anyRequest().authenticated()
            )
            // JWT 필터 추가
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
