package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.DeviceAddReq;
import com.agonylua.smartkitchen.common.DeviceBindReq;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.dto.DeviceDTO;
import com.agonylua.smartkitchen.service.DeviceService;
import com.agonylua.smartkitchen.service.mqtt.MqttService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加设备
     * 前端传: {"homeId": "xxx", "deviceName": "我的电饭煲", "deviceType": "RICE_COOKER"}
     */
    @PostMapping("/add")
    public ApiResponse<String> addDevice(@RequestBody DeviceAddReq req) {
        deviceService.addDevice(req.getHomeId(), req.getDeviceName(), req.getDeviceType());
        return ApiResponse.success("设备添加成功");
    }

    /**
     * 获取家庭下的设备列表
     * 返回: List<DeviceDTO>，其中 deviceData 已经是 Map 对象
     */
    @GetMapping("/list/{homeId}")
    public ApiResponse<List<DeviceDTO>> getDeviceList(@PathVariable String homeId) {
        List<Device> devices = deviceRepository.findByHomeId(homeId);

        // 利用 Stream API 批量转换 Entity -> DTO
        List<DeviceDTO> dtoList = devices.stream()
                .map(DeviceDTO::fromEntity)
                .collect(Collectors.toList());
        log.info("Device list for homeId {}: {}", homeId, dtoList);
        return ApiResponse.success(dtoList);
    }

    /**
     * 控制设备
     * 前端传: {"deviceSn": "SN123456", "payload": {"switch": "on"}}
     */
    @PostMapping("/control")
    public ApiResponse<String> controlDevice(@RequestBody Map<String, String> payload) {
        mqttService.sendCmdMessage(payload);
        return ApiResponse.success("指令已下发");
    }

    /**
     * 绑定设备
     * 前端传: {"deviceSn": "SN123456", "homeId": "xxx"}
     */
    @PostMapping("/bind")
    public ApiResponse<Integer> bindDevice(@RequestBody DeviceBindReq req) {
        int result = deviceService.bindDevice(req.getDeviceSn(), req.getHomeId());
        return ApiResponse.success(result);
    }

    @PostMapping("/unBind")
    public ApiResponse<Boolean> unBindDevice(@RequestBody DeviceBindReq req) {
        return ApiResponse.success(deviceService.unBindDevice(req.getDeviceSn(), req.getHomeId(), req.getUserId()));
    }

    /**
     * 获取家庭下所有设备的近7天功耗数据
     *
     * @param homeId
     * @return
     */
    @GetMapping("/power")
    public ApiResponse<List<Map<String, Object>>> getDevicesPower(@RequestParam String homeId) {
        List<Device> devices = deviceRepository.findByHomeId(homeId);

        // 用于汇总家庭所有设备的每日功耗，TreeMap 自动按日期排序
        Map<String, Double> homeAggregatedPower = new TreeMap<>();

        for (Device device : devices) {
            String powerJson = device.getDevicePower();
            if (powerJson != null && powerJson.trim().startsWith("{")) {
                try {
                    // 解析当前设备的 JSON {"2023-10-25": 1.2, "2023-10-26": 2.1}
                    Map<String, Double> devicePowerMap = objectMapper.readValue(
                            powerJson, new TypeReference<TreeMap<String, Double>>() {
                            }
                    );

                    // 汇总到家庭大集合中
                    devicePowerMap.forEach((date, kwh) -> {
                        homeAggregatedPower.merge(date, kwh, Double::sum);
                    });
                } catch (Exception e) {
                    // 忽略 JSON 格式不正确的脏数据
                }
            }
        }

        // 组装成前端图表需要的 List 格式: [{"date": "2023-10-25", "totalKwh": 5.4}, ...]
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : homeAggregatedPower.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("totalKwh", entry.getValue());
            resultList.add(item);
        }

        return ApiResponse.success(resultList);
    }

    @PostMapping("/updateStatus")
    public ApiResponse<String> updateStatus(@RequestParam String deviceSn) {
        deviceService.updateDeviceStatus(deviceSn);
        return ApiResponse.success("更新成功");
    }

}