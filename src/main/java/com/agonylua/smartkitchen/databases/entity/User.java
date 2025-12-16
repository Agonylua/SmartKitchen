package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    private String userId; // 手动赋值，不使用 @GeneratedValue

    @Column(nullable = false, unique = true)
    private String username; // 业务逻辑需校验只能含字母数字

    @Column(nullable = false)
    private String password;

    private String nickname; // 允许为 NULL
    private String avatarUrl;

    @CreationTimestamp
    private LocalDateTime createTime;
    @UpdateTimestamp
    private LocalDateTime updateTime;
}