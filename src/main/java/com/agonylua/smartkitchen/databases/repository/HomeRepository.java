package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeRepository extends JpaRepository<Home, String> {
    List<Home> findByHomeId(String homeId);

    Optional<Home> findByOwnerId(String ownerId);
}
