package com.example.board.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 'user'는 SQL 예약어라 이름을 바꿔줌
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // ✅ 여기에 닉네임 필드 추가
    @Column(nullable = false, unique = true)
    private String nickname;

    public User() {}


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }
}
