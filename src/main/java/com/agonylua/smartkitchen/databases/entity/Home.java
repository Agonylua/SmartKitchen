package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "home")
public class Home {
    @Id
    private String homeId;

    @Column(nullable = false)
    private String homeName;

    @Column(nullable = false)
    private String ownerId;

    // 将 JSON 数组映射为 Java List<String>
    // 需要配合 JPA Converter 或 Hibernate Types (这里用 String 简化演示，实际建议用 List)
    @Column(columnDefinition = "json")
    private String memberIds;

    @CreationTimestamp
    private LocalDateTime createTime;
}
