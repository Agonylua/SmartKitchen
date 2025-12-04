package com.agonylua.smartkitchen.databases.entity;

import com.agonylua.smartkitchen.controller.common.ApiResponse;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    private String name;
    private String status;
    private boolean isOnline;

    private LocalDateTime last_heartbeat;

}