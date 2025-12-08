package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "automation")
public class Automation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sceneId;

    private Long homeId;
    private String sceneName;

    private Integer triggerType; // 1-手动, 2-定时, 3-条件
    private String triggerCondition;

    private Boolean isEnable; // 对应数据库 tinyint(1)
}