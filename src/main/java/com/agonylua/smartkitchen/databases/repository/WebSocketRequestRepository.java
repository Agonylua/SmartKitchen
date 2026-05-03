package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.WebSocketRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebSocketRequestRepository extends JpaRepository<WebSocketRequest, Long> {
    List<WebSocketRequest> findByOwnerIdAndStatus(String ownerId, Integer status);
}
