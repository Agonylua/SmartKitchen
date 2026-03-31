package com.agonylua.smarthome.utils;

import androidx.annotation.NonNull;

import com.agonylua.smarthome.database.entity.Rules;
import com.agonylua.smarthome.dto.AutomationRuleDTO;
import com.agonylua.smarthome.dto.RuleAction;
import com.agonylua.smarthome.dto.RuleCondition;

import java.util.ArrayList;
import java.util.List;

public class RuleMapper {
    private static final String TAG = "RuleMapper";

    public static List<Rules> toRulesList(List<AutomationRuleDTO> ruleDTOs, String userId) {
        List<Rules> rulesList = new ArrayList<>();
        if (ruleDTOs != null) {
            for (AutomationRuleDTO dto : ruleDTOs) {
                rulesList.add(toRules(dto, userId));
            }
        }
        return rulesList;
    }

    public static Rules toRules(@NonNull AutomationRuleDTO dto, String userId) {
        Rules rule = new Rules();
        rule.setRuleId(dto.getRuleId());
        rule.setUserId(userId);
        rule.setRuleName(dto.getRuleName() != null ? dto.getRuleName() : "");
        rule.setEnable(dto.isEnable());

        RuleCondition condition = dto.getCondition();
        if (condition != null) {
            rule.setConditionType(condition.getType());
            rule.setConditionDeviceSn(condition.getDeviceSn());
            rule.setConditionProperty(condition.getProperty());
            rule.setConditionOperator(condition.getOperator());
            rule.setConditionValue(condition.getValue());
        }

        RuleAction action = dto.getAction();
        if (action != null) {
            rule.setActionDeviceSn(action.getDeviceSn());
            rule.setActionCommand(action.getCommand());
            rule.setActionPayload(action.getPayload());
        }

        rule.setCreatedAt(System.currentTimeMillis());

        return rule;
    }
}

