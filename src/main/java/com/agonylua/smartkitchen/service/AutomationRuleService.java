package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.AutomationRule;
import com.agonylua.smartkitchen.databases.repository.AutomationRuleRepository;
import com.agonylua.smartkitchen.dto.AutomationRuleDTO;
import com.agonylua.smartkitchen.utils.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutomationRuleService {

    @Autowired
    private AutomationRuleRepository ruleRepository;

    public Boolean createRule(String userId, AutomationRuleDTO dto) {
        if (userId == null) {
            return false;
        }

        // 获取该用户下所有的规则，检查规则ID是否重复
        List<AutomationRule> existingRules = ruleRepository.findByUserId(userId);
        boolean isDuplicateId = existingRules.stream()
                .anyMatch(r -> r.getRuleId().equals(dto.getRuleId()));
        if (isDuplicateId) {// 规则ID重复，返回失败
            return false;
        }
        boolean isDuplicateName = existingRules.stream()
                .anyMatch(r -> r.getRuleId().equals(dto.getRuleId()));

        AutomationRule rule = new AutomationRule();
        rule.setRuleId(IdUtil.generateRulesId());
        rule.setUserId(userId);
        if (isDuplicateName) {// 规则名重复
            rule.setRuleName(dto.getRuleId());
        }
        rule.setRuleName(dto.getRuleName());
        rule.setIsEnable(dto.getIsEnable());

        // 映射 Condition
        if (dto.getCondition() != null) {
            rule.setConditionType(dto.getCondition().getType());
            rule.setConditionDeviceSn(dto.getCondition().getDeviceSn());
            rule.setConditionProperty(dto.getCondition().getProperty());
            rule.setConditionOperator(dto.getCondition().getOperator());
            rule.setConditionValue(dto.getCondition().getValue());
        }

        // 映射 Action
        if (dto.getAction() != null) {
            rule.setActionDeviceSn(dto.getAction().getDeviceSn());
            rule.setActionCommand(dto.getAction().getCommand());
            rule.setActionPayload(dto.getAction().getPayload());
        }

        ruleRepository.save(rule);
        return true;
    }

    public List<AutomationRuleDTO> getRulesDtoList(String userId) {
        List<AutomationRuleDTO> dtoList = new ArrayList<>();
        List<AutomationRule> ruleList = ruleRepository.findByUserId(userId);
        for (AutomationRule rule : ruleList) {
            AutomationRuleDTO dto = new AutomationRuleDTO();
            dto.setRuleId(rule.getRuleId());
            dto.setRuleName(rule.getRuleName());
            dto.setIsEnable(rule.getIsEnable());

            // 映射 Condition
            if (rule.getConditionType() != null) {
                AutomationRuleDTO.RuleCondition condition = new AutomationRuleDTO.RuleCondition();
                condition.setType(rule.getConditionType());
                condition.setProperty(rule.getConditionProperty());
                condition.setOperator(rule.getConditionOperator());
                condition.setValue(rule.getConditionValue());
                dto.setCondition(condition);
            }

            // 映射 Action
            if (rule.getActionDeviceSn() != null) {
                AutomationRuleDTO.RuleAction action = new AutomationRuleDTO.RuleAction();
                action.setDeviceSn(rule.getActionDeviceSn());
                action.setCommand(rule.getActionCommand());
                action.setPayload(rule.getActionPayload());
                dto.setAction(action);
            }
            dtoList.add(dto);
        }
        return dtoList;
    }

    public Boolean deleteRule(String userId, String ruleId) {
        if (userId == null || ruleId == null) {
            return false;
        }

        List<AutomationRule> rule = ruleRepository.findByRuleId(ruleId);
        for (AutomationRule r : rule) {
            if (!r.getUserId().equals(userId)) {
                return false;
            }
            ruleRepository.delete(r);
        }
        return true;
    }
}