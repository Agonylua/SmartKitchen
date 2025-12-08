package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "home_member")
public class HomeMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private Long homeId;
    private Long userId;

    private Integer role; // 1-管理员, 2-成员, 3-访客
}
