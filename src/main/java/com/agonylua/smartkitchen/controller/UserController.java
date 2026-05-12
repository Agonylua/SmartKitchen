package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.RegisterReq;
import com.agonylua.smartkitchen.common.UserReq;
import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.dto.UserDTO;
import com.agonylua.smartkitchen.service.UserService;
import com.agonylua.smartkitchen.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;


    /**
     * 用户注册
     * 逻辑：创建用户 -> 自动创建家庭 -> 返回用户信息
     */
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@RequestBody RegisterReq req) {
        log.info("[用户控制器] 收到用户注册请求: userName={}", req.getUsername());
        User user = userService.register(req.getUsername(), req.getPassword(), req.getNickname());
        return ApiResponse.success(UserDTO.fromEntity(user));
    }

    /**
     * 用户登录
     * 实际项目中应该校验密码加密，并生成 JWT Token
     */
    @PostMapping("/login")
    public ApiResponse<UserDTO> login(@RequestBody UserReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到用户登录请求: {}", userId);
        UserDTO user = userService.login(req);
        return ApiResponse.success(user);
    }

    /**
     * token 验证接口
     * 用于前端验证 token 是否有效
     */
    @PostMapping("/validateToken")
    public ApiResponse<Void> verifyToken() {
        log.info("[用户控制器] 收到验证 token 请求");
        return ApiResponse.success(null);
    }

    @GetMapping("/info")
    public ApiResponse<UserDTO> getUserInfo() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到用户信息获取请求: userId={}", userId);
        UserDTO dot = userRepository.findByUserId(userId)
                .map(UserDTO::fromEntity)
                .orElse(null);
        return ApiResponse.success(dot);
    }

    @GetMapping("/list")
    public ApiResponse<List<UserDTO>> getUserListInfo(@RequestParam("homeId") String homeId) {
        log.info("[用户控制器] 收到家庭用户信息获取请求: homeId={}", homeId);
        List<UserDTO> dot = userService.findAllByHomeId(homeId);
        return ApiResponse.success(dot);
    }

    @PostMapping("/exitHome")
    public ApiResponse<String> exitHome(@RequestParam("homeId") String homeId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到用户退出家庭请求: userId={}, oldHomeId={}", userId, homeId);
        String result = userService.exitHome(homeId, userId);
        return ApiResponse.success(result);
    }

    @PostMapping("/updateAvatar")
    public ApiResponse<String> updateAvatar(@RequestParam("file") MultipartFile avatarFile) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到更新用户头像请求: userId={}", userId);
        String avatarUrl = userService.uploadAvatarFile(userId, avatarFile);
        return ApiResponse.success(avatarUrl);
    }

    @PostMapping("/updateNickname")
    public ApiResponse<String> updateNickname(@RequestParam("nickName") String newNickname) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到更新昵称请求: userId={}, newNickname={}", userId, newNickname);
        return userRepository.findByUserId(userId)
                .map(user -> {
                    user.setNickname(newNickname);
                    userRepository.save(user);
                    return ApiResponse.success(newNickname);
                })
                .orElse(ApiResponse.error(null));
    }

    @PostMapping("/resetPassword")
    public ApiResponse<String> resetPassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("[用户控制器] 收到重置密码请求: userId={}", userId);
        userRepository.findByUserId(userId)
                .ifPresentOrElse(user -> {
                    if (user.getPassword().equals(oldPassword)) {
                        user.setPassword(newPassword);
                        userRepository.save(user);
                    } else {
                        throw new RuntimeException("旧密码错误");
                    }
                }, () -> {
                    throw new RuntimeException("用户不存在");
                });
        return ApiResponse.success("密码重置成功");
    }
}