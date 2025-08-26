package com.example.board.dto;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String nickname;

    public LoginResponse(String accessToken, String refreshToken, String userId, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.nickname = nickname;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
}
