package com.agonylua.smartkitchen.databases.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device")
public class Device {
    @Id
    private String deviceSn; // 12位纯数字

    @Column(nullable = false)
    private String homeId;

    @Column(nullable = false)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType; // 使用枚举限制值

    @Column(nullable = false)
    private Boolean deviceStatus; // true: 在线, false: 离线

    @Column(columnDefinition = "json")
    private String deviceData; // 允许为 NULL

    @UpdateTimestamp
    private LocalDateTime updateTime;
}