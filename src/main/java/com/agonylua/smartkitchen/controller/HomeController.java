package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.dto.HomeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeRepository homeRepository;

    /**
     * 获取我的家庭信息(根据户主ID)
     */
    @GetMapping("/owner/{userId}")
    public ApiResponse<HomeDTO> getMyHome(@PathVariable String userId) {
        return homeRepository.findByOwnerId(userId)
                .map(home -> ApiResponse.success(HomeDTO.fromEntity(home)))
                .orElse(ApiResponse.error("未找到相关家庭信息"));
    }
}
