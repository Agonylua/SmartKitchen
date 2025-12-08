package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.HomeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeMemberRepository extends JpaRepository<HomeMember, Long> {
    List<HomeMember> findByUserId(Long userId); // 查找用户加入的所有家庭

    List<HomeMember> findByHomeId(Long homeId); // 查找家庭下的所有成员
}
