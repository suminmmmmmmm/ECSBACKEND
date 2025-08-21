package com.example.board.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.board.dto.TokenResponse;
import com.example.board.entity.User;
import com.example.board.jwt.JwtTokenProvider;
import com.example.board.jwt.RefreshTokenStore;
import com.example.board.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 모든 필요한 컴포넌트 주입
    public AuthService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public void signup(String userId, String password, String nickname) {
        if (userRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public TokenResponse login(String userId, String password) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        refreshTokenStore.save(userId, refreshToken);

        String message = String.format("%s님이 로그인 하셨습니다.", user.getNickname());
        return new TokenResponse(message, accessToken, refreshToken);
    }

    public void logout(String userId) {
        refreshTokenStore.delete(userId);
    }
    public void withdraw(String userId, String rawPassword) {
    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }
    refreshTokenStore.delete(userId);

    // 2) 유저 삭제(하드 삭제)
    userRepository.delete(user);

    // PIN 관련 메서드 전부 제거
    // setPin(...)
    // verifyPin(...)
    // loginWithPin(...)
    }}
