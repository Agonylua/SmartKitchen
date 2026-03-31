package com.agonylua.smartkitchen.dto;

import lombok.Data;

@Data
public class AutomationRuleDTO {
    private String ruleId;
    private String ruleName;
    private Boolean isEnable;
    private RuleCondition condition;
    private RuleAction action;

    @Data
    public static class RuleCondition {
        private String type;
        private String deviceSn;
        private String property;
        private String operator;
        private String value;
    }

    @Data
    public static class RuleAction {
        private String type;
        private String deviceSn;
        private String command;
        private String payload;
    }
}