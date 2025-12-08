package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // 支持分页查询日志，按时间倒序
    Page<Log> findByHomeId(Long homeId, Pageable pageable);

    // 查询某个设备的日志
    List<Log> findByDeviceId(Long deviceId);
}

