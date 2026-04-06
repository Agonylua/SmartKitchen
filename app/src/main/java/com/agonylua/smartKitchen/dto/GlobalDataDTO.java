package com.agonylua.smartKitchen.dto;

import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.database.entity.Home;

import java.util.List;

public class GlobalDataDTO {
    private List<AutomationRuleDTO> automationRules;
    private DevicePowerDTO devicePower;
    private Home home;
    private UserDTO userDTO;
    private Device device;
}
