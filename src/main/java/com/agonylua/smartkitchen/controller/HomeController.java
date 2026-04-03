package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.dto.HomeDTO;
import com.agonylua.smartkitchen.service.HomeService;
import com.agonylua.smartkitchen.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeRepository homeRepository;
    private final HomeService homeService;

    /**
     * 获取我的家庭信息(根据户主ID)
     */
    @GetMapping("/info/{homeId}")
    public ApiResponse<HomeDTO> getMyHome(@PathVariable String homeId) {
        log.info("▶️ [家庭控制器] 收到获取家庭信息请求: homeId={}", homeId);
        return homeRepository.findByHomeId(homeId)
                .map(home -> ApiResponse.success(HomeDTO.fromEntity(home)))
                .orElse(ApiResponse.error("未找到相关家庭信息"));
    }

    @PostMapping("/removeMember")
    public ApiResponse<HomeDTO> removeMember(@RequestParam("homeId") String homeId, @RequestParam("userId") String userId) {
        HomeDTO result = homeService.removeMember(homeId, userId);
        log.info("▶️ [家庭控制器] 收到移除家庭成员请求, 移除后结果: {}", result);
        if (result == null) {
            return ApiResponse.error("移除失败，家庭不存在或用户不在家庭中");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/joinHome")
    public ApiResponse<String> joinHome(@RequestParam("homeId") String homeId) {
        String userId = SecurityUtils.getCurrentUserId();
        String message = homeService.joinHome(homeId, userId);
        return ApiResponse.success(message);
    }

    @PostMapping("/joinHomeApproval")
    public ApiResponse<String> joinHomeApproval(@RequestParam("result") Boolean result, @RequestParam("ownerId") String ownerId, @RequestParam("memberId") String memberId) {
        log.info("▶️ [家庭控制器] 收到家庭加入审批结果: result={}, ownerId={}, memberId={}", result, ownerId, memberId);
        homeService.joinHomeApproval(result, ownerId, memberId);
        return ApiResponse.success("处理完成");
    }
}
