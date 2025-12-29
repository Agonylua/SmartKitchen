package com.agonylua.smarthome.network;

public class LoginResponse {
    // 字段名必须和后端 JSON key 一致，或者使用 @SerializedName("token")
    private String token;
    private String username;
    private long expiresIn;

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    // 如果需要其他字段的 Getter...
}