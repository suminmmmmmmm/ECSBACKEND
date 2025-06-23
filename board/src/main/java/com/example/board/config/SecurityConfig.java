package com.example.board.config;

import com.example.board.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/**").permitAll()
//                        .requestMatchers("/api/boards/**").permitAll() // 모든 회원 조회가능
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/api/boards/**").authenticated() //로그인 한 회원만 가능
                                .requestMatchers("/scraps", "/scraps/**", "/users/**").authenticated() // 스크랩 인증 필요
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() //Swagger 허용
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // 필터 등록

        return http.build();
    }
}
