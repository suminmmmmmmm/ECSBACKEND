package com.example.board.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.board.dto.LoginResponse;
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

    public AuthService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public void signup(String userId, String password, String nickname) {
        if (userRepository.existsByUserId(userId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public LoginResponse login(String userId, String rawPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(user.getUserId()); // subject=userId
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        refreshTokenStore.save(user.getUserId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, user.getUserId(), user.getNickname());
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
        userRepository.delete(user); // 하드 삭제
    }
}
