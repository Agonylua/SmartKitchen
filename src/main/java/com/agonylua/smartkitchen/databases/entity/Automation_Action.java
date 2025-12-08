package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "automation_action")
public class Automation_Action extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    private Long sceneId;
    private Long deviceId;

    @Column(columnDefinition = "json")
    private String actionParams; // 例如 {"switch":"on"}

    private Integer delaySeconds;
    private Integer executionWeight;
}