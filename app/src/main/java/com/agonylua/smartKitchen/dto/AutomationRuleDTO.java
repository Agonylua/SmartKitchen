package com.agonylua.smartKitchen.dto;

public class AutomationRuleDTO {
    private String ruleId;
    private String ruleName;
    private boolean isEnable;
    private RuleCondition condition;
    private RuleAction action;

    public AutomationRuleDTO(String ruleName, boolean isEnable, RuleCondition condition, RuleAction action) {
        this.ruleName = ruleName;
        this.isEnable = isEnable;
        this.condition = condition;
        this.action = action;
    }


    public String getRuleName() {
        return ruleName;
    }


    public boolean isEnable() {
        return isEnable;
    }


    public RuleCondition getCondition() {
        return condition;
    }


    public RuleAction getAction() {
        return action;
    }

    public String getRuleId() {
        return ruleId;
    }

}