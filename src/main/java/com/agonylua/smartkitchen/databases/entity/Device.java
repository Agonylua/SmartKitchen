package com.agonylua.smartkitchen.databases.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "device")
public class Device extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    @Column(unique = true, nullable = false)
    private String deviceSn;

    private Long homeId;
    private Long roomId; // 可以为空

    private String deviceName;

    // 类型: INDUCTION_COOKER, FRIDGE, etc.
    private String deviceType;

    // 存储 JSON 字符串。
    // 在 Service 层可以使用 Jackson ObjectMapper 将其转为 Map 或 Object
    @Column(columnDefinition = "json")
    private String deviceData;
}