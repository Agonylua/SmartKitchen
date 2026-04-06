package com.agonylua.smartKitchen.model;

import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String homeId;

    public User(Map<String, String> users) {
        this.userId = users.get("userId");
        this.nickname = users.get("nickName");
        this.username = users.get("userName");
        this.avatarUrl = users.get("avatarUrl");
        this.homeId = users.get("homeId");
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getHomeId() {
        return homeId;
    }
}
