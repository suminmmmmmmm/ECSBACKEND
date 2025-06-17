package com.example.userservice.jwt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void save(String username, String token) {
        store.put(username, token);
    }

    public boolean isValid(String username, String token) {
        return token.equals(store.get(username));
    }

    public void delete(String username) {
        store.remove(username);
    }
}
