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
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스/페이지 허용 👇
                .requestMatchers("/", "/index.html", "/suggest-demo.html",
                                 "/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                // API 허용
                .requestMatchers(HttpMethod.GET, "/api/suggest").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/suggest/learn").permitAll()

                // 기존 인증 예외
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/check-userid", "/auth/check-nickname").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
