package com.agonylua.smarthome.model;

import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String nickname;
    private String homeName;

    public User(Map<String, String> users) {
        this.userId = users.get("userId");
        this.nickname = users.get("nickName");
        this.username = users.get("userName");
    }

    public String getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }

    public String getHomeName() {
        return homeName;
    }
    public String getNickname() {
        return nickname;
    }
}
