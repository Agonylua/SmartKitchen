package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "home")
public class Home extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long homeId;

    private String homeName;
    private Long ownerId; // 也可以映射为 @ManyToOne SysUser
}
