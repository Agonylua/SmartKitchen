package com.agonylua.smarthome.dto;

public class UserDTO {
    private String userId;
    private String username;
    private String nickname;
    private String homeId;
    private String AvatarUrl;
    private String token;


    public String getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public String getNickname() {
        return nickname;
    }

    public String getHomeId() {
        return homeId;
    }
    public String getAvatarUrl() {
        return AvatarUrl;
    }
    public String getToken() {
        return token;
    }
}
