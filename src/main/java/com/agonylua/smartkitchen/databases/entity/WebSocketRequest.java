package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "websocket_request")
public class WebSocketRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String applicantId; // 申请人ID
    private String ownerId;     // 目标户主ID
    private String homeId;      // 目标家庭ID

    // 状态: 0=待处理(挂起), 1=已同意, 2=已拒绝
    private Integer status = 0;

    private Date createTime = new Date();
}
