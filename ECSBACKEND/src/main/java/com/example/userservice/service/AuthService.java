package com.example.userservice.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.userservice.dto.TokenResponse;
import com.example.userservice.entity.User;
import com.example.userservice.jwt.JwtTokenProvider;
import com.example.userservice.jwt.RefreshTokenStore;
import com.example.userservice.repository.UserRepository;



@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ✅ 모든 필요한 컴포넌트 주입
    public AuthService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public void signup(String username, String password, String nickname) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public TokenResponse login(String username, String password) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    String accessToken = jwtTokenProvider.generateAccessToken(username);
    String refreshToken = jwtTokenProvider.generateRefreshToken(username);

    refreshTokenStore.save(username, refreshToken);

    String message = String.format("%s님이 로그인 하셨습니다.", user.getNickname());
    return new TokenResponse(message, accessToken, refreshToken);
}

}
