package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.controller.common.ApiResponse;
import com.agonylua.smartkitchen.controller.dto.DeviceControlReq;
import com.agonylua.smartkitchen.controller.dto.DeviceDTO;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.databases.repository.RoomRepository;
import com.agonylua.smartkitchen.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor // 自动注入 Repository
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;

    // 假设你有一个 MqttService 用于发送消息
    // private final MqttService mqttService;

    /**
     * 1. 获取家庭下的所有设备列表
     */
    @GetMapping("/home/{homeId}")
    public ApiResponse<List<DeviceDTO>> getHomeDevices(@PathVariable Long homeId) {
        List<Device> devices = deviceRepository.findByHomeId(homeId);

        // Entity -> DTO 转换
        List<DeviceDTO> dtoList = devices.stream().map(device -> {
            DeviceDTO dto = new DeviceDTO();
            dto.setDeviceId(device.getDeviceId());
            dto.setDeviceSn(device.getDeviceSn());
            dto.setDeviceName(device.getDeviceName());
            dto.setDeviceType(device.getDeviceType());

            // 查找房间名 (实际项目中建议用 Map 缓存或 SQL 联查优化)
            if (device.getRoomId() != null) {
                roomRepository.findById(device.getRoomId())
                        .ifPresent(room -> dto.setRoomName(room.getRoomName()));
            }

            // 核心：利用工具类把 JSON 字符串转为 Map
            dto.setStatus(JsonUtils.parseMap(device.getDeviceData()));

            // TODO: 从 Redis 或心跳记录判断是否在线
            dto.setIsOnline(true);

            return dto;
        }).collect(Collectors.toList());

        return ApiResponse.success(dtoList);
    }

    /**
     * 2. 控制设备 (核心功能)
     */
    @PostMapping("/control")
    public ApiResponse<String> controlDevice(@RequestBody DeviceControlReq req) {
        // 1. 校验设备是否存在
        Device device = deviceRepository.findById(req.getDeviceId())
                .orElseThrow(() -> new RuntimeException("设备不存在"));

        // 2. 构造 MQTT 消息
        String topic = "smart/device/" + device.getDeviceSn() + "/set";
        String payloadJson = JsonUtils.toJson(req.getPayload());

        // 3. 发送 MQTT (伪代码)
        // mqttService.publish(topic, payloadJson);
        System.out.println("发送MQTT指令: Topic=" + topic + ", Payload=" + payloadJson);

        // 4. (可选) 乐观更新数据库状态，让 APP 立即看到变化，或者等待设备回调
        // 实际开发中，通常等待设备通过 MQTT 回复状态后再更新 DB

        return ApiResponse.success("指令已发送");
    }

    /**
     * 3. 获取单个设备详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DeviceDTO> getDeviceInfo(@PathVariable Long id) {
        // 逻辑同列表查询，返回单个对象
        return ApiResponse.success(new DeviceDTO());
    }
}