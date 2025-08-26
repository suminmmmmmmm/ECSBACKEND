package com.example.board.repository;


import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);     // username → userId
    Optional<User> findByNickname(String nickname);
    boolean existsByUserId(String userId);          // 중복체크 용
    boolean existsByNickname(String nickname);
}
