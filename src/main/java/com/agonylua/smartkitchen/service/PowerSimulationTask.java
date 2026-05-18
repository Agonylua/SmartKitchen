package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.entity.DeviceMode;
import com.agonylua.smartkitchen.databases.entity.DeviceStatus;
import com.agonylua.smartkitchen.databases.entity.DeviceType;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PowerSimulationTask {

    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // 每分钟执行一次
    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void simulatePowerConsumption() {
        List<Device> devices = deviceRepository.findAll();
        String todayDate = LocalDate.now().toString(); // 当前日期，例如 "2023-10-26"
        LocalDate limitDate = LocalDate.now().minusDays(7); // 7天前的日期界限

        for (Device device : devices) {
            // 只有在线状态才耗电
            if (device.getDeviceStatus() != DeviceStatus.ONLINE) {
                continue;
            }

            // 1. 计算这一分钟的模拟功耗 (度)
            double basePower = getBasePower(device.getDeviceType(), device.getDeviceMode());
            random.nextDouble();

            // 3. 处理 devicePower (维护 7 天 JSON 滑动窗口)
            Map<String, Double> powerHistory = new TreeMap<>();
            String currentPowerJson = device.getDevicePower();

            // 尝试解析原有的 JSON
            if (currentPowerJson != null && currentPowerJson.trim().startsWith("{")) {
                try {
                    powerHistory = objectMapper.readValue(currentPowerJson, new TypeReference<TreeMap<String, Double>>() {
                    });
                } catch (Exception e) {
                    log.warn("解析设备 [{}] 的功耗 JSON 失败，将重新初始化", device.getDeviceSn());
                }
            }

            // 尝试读取设备 runTime (作为累计时间戳/秒数) 来计算当天功耗
            // 用户指出 runTime 是时间戳 (e.g. 2282), 这里理解为累计运行时间(秒)
            String runTimeStr = device.getRunTime();
            double runTimeSeconds;
            if (runTimeStr != null && !runTimeStr.trim().isEmpty()) {
                try {
                    runTimeSeconds = Double.parseDouble(runTimeStr);
                    double totalEnergyToday = (runTimeSeconds / 3600.0) * (basePower / 1000.0);
                    powerHistory.put(todayDate, totalEnergyToday);
                } catch (Exception e) {
                    log.warn("Device {} runTime parse error: {}", device.getDeviceSn(), runTimeStr);
                }
            }
            // 滑动窗口清理：剔除超过 7 天的数据
            powerHistory.entrySet().removeIf(e -> {
                try {
                    return LocalDate.parse(e.getKey()).isBefore(limitDate);
                } catch (Exception ex) {
                    return true; // 格式异常的旧数据也删掉
                }
            });

            // 存回 JSON 字符串并保存到数据库
            try {
                device.setDevicePower(objectMapper.writeValueAsString(powerHistory));
                deviceRepository.save(device);
            } catch (Exception e) {
                log.error("保存设备功耗 JSON 失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 基准功率映射
     */
    private double getBasePower(DeviceType type, String modeStr) {
        if (type == null || modeStr == null) return 5.0;
        try {
            DeviceMode mode = DeviceMode.valueOf(modeStr);
            return switch (type) {
                case REFRIGERATOR -> switch (mode) {
                    case FAST_COOL -> 150.0;
                    case ENERGY_SAVING -> 50.0;
                    case HOLIDAY -> 30.0;
                    default -> 80.0;
                };
                case MICROWAVE -> switch (mode) {
                    case HEAT -> 800.0;
                    case GRILL -> 1000.0;
                    case STEAM -> 900.0;
                    default -> 5.0;
                };
                case DISHWASHER -> switch (mode) {
                    case STANDARD_WASH -> 1200.0;
                    case INTENSIVE_WASH -> 1800.0;
                    case DRY -> 500.0;
                    default -> 5.0;
                };
                case RICE_COOKER -> switch (mode) {
                    case COOK_RICE -> 800.0;
                    case STEAM_COOK -> 900.0;
                    case CAKE -> 600.0;
                    default -> 5.0;
                };
                case STERILIZER -> switch (mode) {
                    case HIGH_TEMP -> 600.0;
                    case STERILIZER_DRY -> 300.0;
                    default -> 5.0;
                };
            };
        } catch (IllegalArgumentException e) {
            return 5.0;
        }
    }
}