package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.DeviceAddReq;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.dto.DeviceDTO;
import com.agonylua.smartkitchen.service.DeviceService;
import com.agonylua.smartkitchen.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;

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

        return ApiResponse.success(dtoList);
    }

    /**
     * 控制设备 (模拟 MQTT 发送)
     * 前端传: {"deviceId": "SN123456", "payload": {"switch": "on"}}
     */
    @PostMapping("/control")
    public ApiResponse<String> controlDevice(@RequestBody Map<String, Object> req) {
        String sn = (String) req.get("deviceId");
        Map<String, Object> payload = (Map<String, Object>) req.get("payload");

        // 1. 校验设备是否存在
        if (!deviceRepository.existsByDeviceSn(sn)) {
            return ApiResponse.error("设备不存在");
        }

        // 2. 构造 MQTT Topic
        // 假设 Topic 格式: home/device/{sn}/set
        String topic = "home/device/" + sn + "/set";
        String msg = JsonUtils.toJson(payload);

        // 3. TODO: 调用 MQTT Client 发送消息
        // mqttClient.publish(topic, msg);
        System.out.println(">>> [MQTT发送] Topic: " + topic + " Payload: " + msg);

        return ApiResponse.success("指令已下发");
    }
}