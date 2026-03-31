package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeRepository extends JpaRepository<Home, String> {
    Optional<Home> findByHomeId(String homeId);

    Optional<Home> findByOwnerId(String ownerId);

    @Query(value = "SELECT * FROM home WHERE member_ids LIKE CONCAT('%', :userId, '%')", nativeQuery = true)
    Optional<Home> findByMemberId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM home WHERE owner_id = :userId OR member_ids LIKE CONCAT('%', :userId, '%')", nativeQuery = true)
    Optional<Home> findByOwnerIdOrMemberIds(@Param("userId") String userId);
}
