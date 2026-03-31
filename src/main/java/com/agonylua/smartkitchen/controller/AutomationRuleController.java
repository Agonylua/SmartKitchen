package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.dto.AutomationRuleDTO;
import com.agonylua.smartkitchen.service.AutomationRuleService;
import com.agonylua.smartkitchen.service.SceneExecutionService;
import com.agonylua.smartkitchen.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rules")
public class AutomationRuleController {

    @Autowired
    private AutomationRuleService ruleService;
    @Autowired
    private SceneExecutionService sceneExecutionService;

    @PostMapping("/create")
    public ApiResponse<Boolean> addAutomationRule(@RequestBody AutomationRuleDTO ruleDTO) {
        log.info("▶️ [规则控制器] 收到创建规则请求: {}", ruleDTO);
        String userId = SecurityUtils.getCurrentUserId();

        return ApiResponse.success(ruleService.createRule(userId, ruleDTO));
    }

    @GetMapping("/list")
    public ApiResponse<List<AutomationRuleDTO>> getAutomationRules(@RequestParam String userId) {
        List<AutomationRuleDTO> ruleList = ruleService.getRulesDtoList(userId);
        return ApiResponse.success(ruleList);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteAutomationRule(@RequestParam String ruleId) {
        log.info("▶️ [规则控制器] 收到删除规则请求: {}", ruleId);
        String userId = SecurityUtils.getCurrentUserId();

        return ApiResponse.success(ruleService.deleteRule(userId, ruleId));
    }

    @PostMapping("/scene")
    public ApiResponse<Boolean> executeScene(@RequestParam String ruleMode) {
        log.info("▶️ [规则控制器] 收到执行场景请求: {}", ruleMode);
        String userId = SecurityUtils.getCurrentUserId();

        return ApiResponse.success(sceneExecutionService.executeManualScene(userId, ruleMode));
    }
}