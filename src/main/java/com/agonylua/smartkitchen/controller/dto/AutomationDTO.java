package com.agonylua.smartkitchen.controller.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AutomationDTO {
    private Long sceneId;
    private String sceneName;
    private Boolean isEnable;
    private List<Automation_ActionDTO> actions; // 嵌套动作列表

    @Data
    public static class Automation_ActionDTO {
        private Long deviceId;
        private String deviceName;
        private Map<String, Object> actionParams; // 动作参数
        private Integer delaySeconds;
    }
}