package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "log")
public class Log extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private Long homeId;

    // 下面三个字段可为空，建议使用包装类 Long 而不是 long
    private Long userId;
    private Long sceneId;
    private Long deviceId;

    private Integer logType; // 1-控制, 2-自动化, 3-警报
    private String logDetail;
}
