package com.agonylua.smartKitchen.common;

public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;

    public RegisterRequest(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
