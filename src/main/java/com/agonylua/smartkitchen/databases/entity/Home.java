package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "json")
    private List<String> memberIds = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createTime;
}
