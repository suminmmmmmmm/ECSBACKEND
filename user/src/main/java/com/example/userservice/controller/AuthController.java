package com.example.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.TokenResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam String nickname) {
        try {
            authService.signup(username, password, nickname);
            return ResponseEntity.ok("회원가입 완료!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<String> checkNickname(@RequestParam String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 닉네임입니다.");
        }
        return ResponseEntity.ok("사용 가능한 닉네임입니다.");
    }
    @PostMapping("/login")
public ResponseEntity<?> login(@RequestParam String username,
                               @RequestParam String password) {
    try {
        TokenResponse result = authService.login(username, password);
        return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
        // 메시지를 함께 전달
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(e.getMessage());
    }
}


}


    

