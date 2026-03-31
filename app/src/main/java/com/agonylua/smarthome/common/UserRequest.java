package com.agonylua.smarthome.common;

public class UserRequest {
    private String password;
    private String nickname;
    private String homeId;
    private String avatarUrl;

    // 构造函数
    public UserRequest(String homeId, String password) {
        this.homeId = homeId;
        this.password = password;
    }

    public UserRequest(String homeId, String nickname, String avatarUrl) {
        this.homeId = homeId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    // Getters and Setters (Retrofit/Gson 需要)
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

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
