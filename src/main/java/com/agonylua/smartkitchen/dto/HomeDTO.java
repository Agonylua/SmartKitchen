package com.agonylua.smartkitchen.dto;

import com.agonylua.smartkitchen.databases.entity.Home;
import com.agonylua.smartkitchen.utils.JsonUtil;
import lombok.Data;

import java.util.List;

@Data
public class HomeDTO {
    private String homeId;
    private String homeName;
    private String ownerId;

    // 重点：前端看到的是数组 ["user1", "user2"]，而不是字符串 "[\"user1\"...]"
    private List<String> memberIds;

    public static HomeDTO fromEntity(Home home) {
        HomeDTO dto = new HomeDTO();
        dto.setHomeId(home.getHomeId());
        dto.setHomeName(home.getHomeName());
        dto.setOwnerId(home.getOwnerId());

        // 解析数据库里的 JSON 字符串为 List
        if (home.getMemberIds() != null) {
            dto.setMemberIds(JsonUtil.parseList(home.getMemberIds().toString(), String.class));
        }
        return dto;
    }
}