package com.agonylua.smartkitchen.common;

import lombok.Data;

@Data
public class UserReq {
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String userId;
}
