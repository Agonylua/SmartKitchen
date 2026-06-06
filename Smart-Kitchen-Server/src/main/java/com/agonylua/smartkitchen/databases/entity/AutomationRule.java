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
    private String conditionType; // 触发设备SN

    @Column(name = "condition_device_sn")
    private String conditionDeviceSn; // 触发设备SN

    @Column(name = "condition_property")
    private String conditionProperty; // 监听属性

    @Column(name = "condition_operator")
    private String conditionOperator; // 运算符: >, <, ==

    @Column(name = "condition_value")
    private String conditionValue;    // 阈值/比较值

    // === THEN 动作 ===
    @Column(name = "action_device_sn")
    private String actionDeviceSn;    // 目标设备SN

    @Column(name = "action_command")
    private String actionCommand;     // 动作指令

    @Column(name = "action_payload")
    private String actionPayload;     // 指令参数

    @Column(name = "created_at")
    private Date createdAt = new Date();
}