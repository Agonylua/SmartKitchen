package com.agonylua.smartkitchen.common;

import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String password;
    private String nickname; // 可选
}
