package com.agonylua.smartKitchen.dto;

import java.util.List;

public class HomeDTO {
    private String homeId;
    private String homeName;
    private String ownerId;
    private List<String> memberIds;

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

}
