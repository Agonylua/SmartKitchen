package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.AutomationRule;
import com.agonylua.smartkitchen.databases.repository.AutomationRuleRepository;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
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

    // 同一个 Easy Rules 引擎实例
    private final RulesEngine rulesEngine = new DefaultRulesEngine();
    @Autowired
    private AutomationRuleRepository ruleRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Lazy // 防止与 MqttService 产生循环依赖
    @Autowired
    private MqttService mqttService;

    /**
     * 事件驱动入口 (由 MQTT 硬件上报瞬间触发)
     */
    public void processDeviceEvent(String deviceSn, String property, String currentValue) {
        //log.info("[规则引擎] 收到事件: Device={}, Property={}, Value={}", deviceSn, property, currentValue);

        List<AutomationRule> dbRules = ruleRepository.findByConditionDeviceSn(deviceSn);
        if (dbRules.isEmpty()) return;

        Facts facts = new Facts();
        facts.put(property, parseTypedValue(currentValue));

        // 调用公共执行器
        executeRules(dbRules, facts, property);
    }

    /**
     * 时间驱动入口 (由 Spring 定时器每分钟的第 0 秒触发)
     * cron表达式: "0 * * * * ?" 表示每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeEvent() {
        // 获取当前时间，格式化为 HH:mm
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        //log.info("[规则引擎] 引擎扫描当前系统时间: {}", currentTime);

        // 所有时间驱动的规则，触发源统一存为 "TIMER"
        List<AutomationRule> dbRules = ruleRepository.findByConditionType("TIME");
        if (dbRules.isEmpty()) return;

        Facts facts = new Facts();
        facts.put("cron", currentTime); // 将当前时间塞进 MVEL 的变量中

        // 调用公共执行器
        executeRules(dbRules, facts, "cron");
    }

    /**
     * 公共规则构建与发射器
     */
    private void executeRules(List<AutomationRule> dbRules, Facts facts, String targetProperty) {
        Rules rules = new Rules();

        for (AutomationRule dbRule : dbRules) {
            // 只过滤当前关注的属性规则
            if (!targetProperty.equals(dbRule.getConditionProperty())) continue;
            boolean repeat = deviceRepository.findByDeviceSn(dbRule.getActionDeviceSn())
                    .map(value -> value.getDeviceMode() != null && value.getDeviceMode().equals(dbRule.getActionPayload()))
                    .orElse(false);
            if (repeat) {
                //log.info("[规则引擎] 规则[{}]动作被拦截: 目标设备 {} 的模式已经是 {}", dbRule.getRuleName(), dbRule.getActionDeviceSn(), dbRule.getActionPayload());
                continue;
            }
            String mvelExpression = buildMvelExpression(dbRule);
            //log.info("[规则引擎] 准备注册规则: ID={}, 名称={}, 表达式=[{}]", dbRule.getRuleId(), dbRule.getRuleName(), mvelExpression);

            // 预先组装要下发的 MQTT 消息
            Map<String, String> payload = new HashMap<>();
            payload.put("deviceSn", dbRule.getActionDeviceSn());
            payload.put(dbRule.getActionCommand(), dbRule.getActionPayload());

            Rule easyRule = new RuleBuilder()
                    .name("Rule_" + dbRule.getRuleId()) // 使用规则ID作为规则名称，确保唯一性
                    .description(dbRule.getRuleName()) // 将规则名称放在描述中，便于日志输出
                    .when(new MVELCondition(mvelExpression)) // 使用 MVEL 表达式作为条件
                    .then(f -> {
                        log.info("[规则引擎] 规则命中: {}, 条件: [{}]", dbRule.getRuleName(), mvelExpression);
                        log.info("[规则引擎] 下发控制指令给设备 {}: {}", dbRule.getActionDeviceSn(), payload);
                        mqttService.sendCmdMessage(payload);
                    })
                    .build();

            rules.register(easyRule); // 注册规则到引擎中
        }

        // 执行所有满足条件的规则
        if (!rules.isEmpty()) {
            if (!mqttService.isConnected()) {
                log.warn("[规则引擎] 虽然规则非空，但 MQTT 未连接，实际可能无法成功下发指令。");
            }
            rulesEngine.fire(rules, facts); // 触发规则引擎，传入事实数据，执行匹配的规则
        }
    }

    /**
     * 辅助：构建 MVEL 表达式
     */
    private String buildMvelExpression(AutomationRule rule) {
        String prop = rule.getConditionProperty();
        String op = rule.getConditionOperator();
        String val = rule.getConditionValue();

        if (op == null || op.trim().isEmpty()) {
            op = "==";
        } else if ("=".equals(op)) {
            op = "==";
        }

        // 非数值类型（例如 "15:30" 或 "FINISHED"），在 MVEL 中必须加单引号
        if (!isNumeric(val)) {
            return prop + " " + op + " '" + val + "'";
        }

        // 数值比较 (如 temperature > 30)
        return prop + " " + op + " " + val;
    }

    // 尝试将字符串解析为数字，如果失败则原样返回字符串
    private Object parseTypedValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    // 判断字符串是否为数值类型
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}