package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "automation_rules")
public class AutomationRule {
    @Id
    private String ruleId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "is_enable")
    private Boolean isEnable = true;

    // === IF 条件 ===
    @Column(name = "condition_type")
    private String conditionType; // 触发设备SN (如 esp32_01)

    @Column(name = "condition_device_sn")
    private String conditionDeviceSn; // 触发设备SN (如 esp32_01)

    @Column(name = "condition_property")
    private String conditionProperty; // 监听属性 (如 temperature, status)

    @Column(name = "condition_operator")
    private String conditionOperator; // 运算符: >, <, ==

    @Column(name = "condition_value")
    private String conditionValue;    // 阈值 (如 30.0, OFFLINE)

    // === THEN 动作 ===
    @Column(name = "action_device_sn")
    private String actionDeviceSn;    // 目标设备SN

    @Column(name = "action_command")
    private String actionCommand;     // 动作指令 (如 mode)

    @Column(name = "action_payload")
    private String actionPayload;     // 指令参数 (如 auto)

    @Column(name = "created_at")
    private Date createdAt = new Date();
}