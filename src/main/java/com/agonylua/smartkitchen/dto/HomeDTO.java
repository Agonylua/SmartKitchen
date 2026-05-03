package com.agonylua.smartkitchen.dto;

import com.agonylua.smartkitchen.databases.entity.Home;
import lombok.Data;

import java.util.List;

@Data
public class HomeDTO {
    private String homeId;
    private String homeName;
    private String ownerId;

    // 前端看到的是数组 ["user1", "user2"]，而不是字符串 "[\"user1\"...]"
    private List<String> memberIds;

    public static HomeDTO fromEntity(Home home) {
        HomeDTO dto = new HomeDTO();
        dto.setHomeId(home.getHomeId());
        dto.setHomeName(home.getHomeName());
        dto.setOwnerId(home.getOwnerId());

        if (home.getMemberIds() != null) {
            dto.setMemberIds(home.getMemberIds());
        }
        return dto;
    }
}