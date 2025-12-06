package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySn(String sn);
    Optional<Device> findByName(String name);
    List<Device> findByStatus(String status);

    boolean existsBySn(String sn);
    boolean existsByName(String name);

}