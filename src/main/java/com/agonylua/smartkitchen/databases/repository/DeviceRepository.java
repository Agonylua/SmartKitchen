package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByNumber(String number);
    Optional<Device> findByName(String name);
    Optional<Device> findByIsOnline(String number);

    List<Device> findByStatus(String status);

    // 组合查询
    boolean existsByNumber(String number);
    boolean existsByName(String name);
    boolean IsOnline(String number);

}