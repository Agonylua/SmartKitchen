package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String user_id);
    Optional<User> findByUsername(String username);
}
