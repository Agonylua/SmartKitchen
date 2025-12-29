package com.agonylua.smarthome.network;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // Android 发送数据通常只需要构造函数，Gson 会自动读取字段
}
