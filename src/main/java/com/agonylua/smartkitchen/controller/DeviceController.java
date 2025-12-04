package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.controller.common.ApiResponse;
import com.agonylua.smartkitchen.controller.dto.DeviceDto;
import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;

    // 查询所有
    @GetMapping
    public ApiResponse<List<Device>> list() {
        return ApiResponse.success(deviceRepository.findAll());
    }

    // 按 ID 查询
    @GetMapping("/{id}")
    public ApiResponse<Device> getById(@PathVariable Long id) {
        return deviceRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("[get]设备不存在"));
    }

    // 按 Number 查询
    @GetMapping("/number/{number}")
    public ApiResponse<Device> getByNumber(@PathVariable String number) {
        return deviceRepository.findByNumber(number)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("[get]设备不存在"));
    }

    // 按 Name 查询
    @GetMapping("/name/{name}")
    public ApiResponse<Device> getByName(@PathVariable String name) {
        return deviceRepository.findByName(name)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("[get]设备不存在"));
    }

    // 新增
    @PostMapping
    public ApiResponse<Device> create(@Valid @RequestBody DeviceDto dto) {
        if (deviceRepository.existsByNumber((dto.getNumber()))) {
            return ApiResponse.error("[create]设备已存在");
        }

        Device device = new Device();
        device.setNumber(dto.getNumber());
        device.setName(dto.getName());

        return ApiResponse.success(deviceRepository.save(device));
    }

    // 修改
    @PutMapping("/{id}")
    public ApiResponse<Device> update(@PathVariable Long id,
                                      @Valid @RequestBody DeviceDto dto) {
        return deviceRepository.findById(id)
                .map(device -> {
                    device.setName(dto.getName());
                    device.setStatus(dto.getStatus());
                    return ApiResponse.success(deviceRepository.save(device));
                })
                .orElse(ApiResponse.error("[update]设备不存在"));
    }

    // 删除
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        if (!deviceRepository.existsById(id)) {
            return ApiResponse.error("[delete]设备不存在");
        }
        deviceRepository.deleteById(id);
        return ApiResponse.success("删除成功");
    }
}