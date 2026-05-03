package com.agonylua.smartkitchen.dto;

import com.agonylua.smartkitchen.databases.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private String userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String homeId;
    private String token;

    // Entity 转为 DTO
    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}
