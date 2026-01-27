package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.DeviceAddReq;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.dto.DeviceDTO;
import com.agonylua.smartkitchen.service.DeviceService;
import com.agonylua.smartkitchen.service.mqtt.MqttController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final MqttController mqttController;

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
    public ApiResponse<String> controlDevice(@RequestBody String req) {
        mqttController.sendCmdMessage(req);
        return ApiResponse.success("指令已下发");
    }
}