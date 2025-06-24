package com.example.board.service;

import java.util.Collections;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.board.entity.User;
import com.example.board.repository.UserRepository;

@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username으로 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자가 존재하지 않습니다: " + username));

        // Spring Security에서 사용할 User 객체로 변환
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                     // 사용자명
                user.getPassword(),                     // 비밀번호
                Collections.singleton(                  // 권한 리스트 (단일 권한 부여)
                        new SimpleGrantedAuthority("ROLE_USER")
                )
        );
    }
}
