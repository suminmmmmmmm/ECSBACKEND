package com.example.board.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    // 클라이언트는 userId로 보내게 하고,
    // 기존 username 키로 와도 매핑되도록 JsonAlias 추가
    @JsonProperty("userId")
    @JsonAlias({"username"})
    private String userId;

    private String password;

    public LoginRequest() {}

    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}