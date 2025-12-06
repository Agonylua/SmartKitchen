package com.agonylua.smartkitchen.databases.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", updatable = false, nullable = false, length = 36)
    private UUID id;

    @Column(name = "SN", nullable = false, length = 100, unique = true)
    private String sn;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private DeviceStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "Data", columnDefinition = "json", nullable = false)
    private Map<String, Object> data;

    @Column(name = "TIME", nullable = false)
    private LocalDateTime time;

    @PrePersist
    @PreUpdate
    protected void prePersistAndUpdate() {
        if (this.time == null) {
            this.time = LocalDateTime.now();
        }
    }

}