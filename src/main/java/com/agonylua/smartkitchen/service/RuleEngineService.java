package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.AutomationRule;
import com.agonylua.smartkitchen.databases.repository.AutomationRuleRepository;
import com.agonylua.smartkitchen.service.mqtt.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RuleBuilder;
import org.jeasy.rules.mvel.MVELCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RuleEngineService {

    // 复用同一个 Easy Rules 引擎实例
    private final RulesEngine rulesEngine = new DefaultRulesEngine();
    @Autowired
    private AutomationRuleRepository ruleRepository;
    @Lazy // 防止与 MqttService 产生循环依赖
    @Autowired
    private MqttService mqttService;

    /**
     * 🚀 引擎 1：事件驱动入口 (由 MQTT 硬件上报瞬间触发)
     */
    public void processDeviceEvent(String deviceSn, String property, String currentValue) {
        log.debug("▶️ [事件驱动] 收到事件: Device={}, Property={}, Value={}", deviceSn, property, currentValue);

        List<AutomationRule> dbRules = ruleRepository.findByConditionDeviceSn(deviceSn);
        if (dbRules.isEmpty()) return;

        Facts facts = new Facts();
        facts.put(property, parseTypedValue(currentValue));

        // 调用公共执行器
        executeRules(dbRules, facts, property);
    }

    /**
     * ⏰ 引擎 2：时间驱动入口 (由 Spring 定时器每分钟的第 0 秒触发)
     * cron表达式: "0 * * * * ?" 表示每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeEvent() {
        // 获取当前时间，格式化为 HH:mm (例如 "08:00")
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        log.debug("⏰ [时间驱动] 引擎扫描当前系统时间: {}", currentTime);

        // 我们约定：所有时间驱动的规则，触发源统一存为 "TIMER"
        List<AutomationRule> dbRules = ruleRepository.findByConditionType("TIMER");
        if (dbRules.isEmpty()) return;

        Facts facts = new Facts();
        facts.put("time", currentTime); // 将当前时间塞进 MVEL 的变量中

        // 调用公共执行器
        executeRules(dbRules, facts, "time");
    }

    /**
     * ⚙️ 核心：公共规则构建与发射器
     */
    private void executeRules(List<AutomationRule> dbRules, Facts facts, String targetProperty) {
        Rules rules = new Rules();

        for (AutomationRule dbRule : dbRules) {
            // 只过滤当前关注的属性规则
            if (!targetProperty.equals(dbRule.getConditionProperty())) continue;

            String mvelExpression = buildMvelExpression(dbRule);

            // 预先组装要下发的 MQTT 消息
            Map<String, String> payload = new HashMap<>();
            payload.put("deviceSn", dbRule.getActionDeviceSn());
            payload.put(dbRule.getActionCommand(), dbRule.getActionPayload());

            Rule easyRule = new RuleBuilder()
                    .name("Rule_" + dbRule.getRuleId())
                    .description(dbRule.getRuleName())
                    .when(new MVELCondition(mvelExpression))
                    .then(f -> {
                        log.info("✅ [EasyRules命中] 规则: {}, 条件: [{}]", dbRule.getRuleName(), mvelExpression);
                        log.info("🚀 [执行动作] 下发控制指令给设备 {}: {}", dbRule.getActionDeviceSn(), payload);
                        mqttService.sendCmdMessage(payload);
                    })
                    .build();

            rules.register(easyRule);
        }

        // 开火！执行所有满足条件的规则
        if (!rules.isEmpty() && mqttService.isConnected()) {
            rulesEngine.fire(rules, facts);
        }
    }

    /**
     * 辅助：构建 MVEL 表达式
     */
    private String buildMvelExpression(AutomationRule rule) {
        String prop = rule.getConditionProperty();
        String op = rule.getConditionOperator();
        String val = rule.getConditionValue();

        // 字符串比对（如 time == '08:00' 或 status == 'FINISHED'），MVEL 需要加单引号
        if ("==".equals(op) && !isNumeric(val)) {
            return prop + " == '" + val + "'";
        }
        // 数值比较 (如 temperature > 30)
        return prop + " " + op + " " + val;
    }

    private Object parseTypedValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}