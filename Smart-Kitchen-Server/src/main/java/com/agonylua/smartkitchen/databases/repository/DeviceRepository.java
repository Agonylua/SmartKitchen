package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByDeviceSn(String deviceSn);

    List<Device> findByHomeId(String homeId);

    boolean existsByDeviceSn(String deviceSn);
}