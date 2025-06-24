package com.example.board.jwt;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ✅ roles까지 받는 토큰 생성 함수
    public String generateAccessToken(String username, List<String> roles) {
        return buildToken(username, roles, accessExpiration);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, null, refreshExpiration);
    }

    private String buildToken(String username, List<String> roles, long duration) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + duration);

        Claims claims = Jwts.claims().setSubject(username);
        if (roles != null) {
            claims.put("roles", roles);  // 🔥 핵심: roles 포함
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
