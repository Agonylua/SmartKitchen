package com.agonylua.smartkitchen.databases.entity;

import com.agonylua.smartkitchen.databases.converter.StringListConverter;
import jakarta.persistence.*;
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
    @Convert(converter = StringListConverter.class)
    private List<String> memberIds = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createTime;
}
