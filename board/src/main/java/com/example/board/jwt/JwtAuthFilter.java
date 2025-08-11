package com.example.board.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // ✅ 공개 엔드포인트만 스킵
        if (isPublic(request)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();

        if ("/auth/login".equals(uri) && "POST".equals(method)) return true;
        if ("/auth/signup".equals(uri) && "POST".equals(method)) return true;
        if ("/auth/check-username".equals(uri) && "GET".equals(method)) return true;
        if ("/auth/check-nickname".equals(uri) && "GET".equals(method)) return true;

        // swagger 허용
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) return true;

        return false; // 🔒 나머지는 다 필터 타서 JWT 검증
    }
}
