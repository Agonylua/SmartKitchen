package com.agonylua.smartkitchen.databases.repository;

import com.agonylua.smartkitchen.databases.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, String> {
    List<AutomationRule> findByUserId(String userId);

    List<AutomationRule> findByRuleId(String ruleId);

    List<AutomationRule> findByConditionDeviceSn(String deviceSn);

    List<AutomationRule> findByConditionType(String type);
}