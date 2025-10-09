package com.example.board.controller;

import com.example.board.dto.SignupRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.board.dto.LoginResponse;
import com.example.board.repository.UserRepository;
import com.example.board.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

//    @PostMapping("/signup")
//    public ResponseEntity<String> signup(@RequestParam String userId,
//                                         @RequestParam String password,
//                                         @RequestParam String nickname) {
//        try {
//            authService.signup(userId, password, nickname);
//            return ResponseEntity.ok("회원가입 완료!");
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
//        }
//    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // ❌ 유효성 실패 시 첫 번째 에러 메시지 반환
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            authService.signup(request.getUserId(), request.getPassword(), request.getNickname());
            return ResponseEntity.ok("회원가입 완료!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/check-userid")
    public ResponseEntity<String> checkUserId(@RequestParam String userId) {
        if (userRepository.findByUserId(userId).isPresent()) {
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
    public ResponseEntity<?> login(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam String password) {

        String id = (userId != null && !userId.isBlank()) ? userId : username;
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body("userId(username) is required");
        }

        LoginResponse result = authService.login(id, password);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String userId) {
        authService.logout(userId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestParam String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        try {
            authService.withdraw(userId, password);
            return ResponseEntity.ok("회원탈퇴 완료");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}