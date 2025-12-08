package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.Automation_Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Automation_ActionRepository extends JpaRepository<Automation_Action, Long> {
    List<Automation_Action> findBySceneIdOrderByExecutionWeight(Long sceneId);

    void deleteBySceneId(Long sceneId); // 更新场景时可能需要先删除旧动作
}
