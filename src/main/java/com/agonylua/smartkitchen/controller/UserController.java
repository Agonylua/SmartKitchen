package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.RegisterReq;
import com.agonylua.smartkitchen.common.UserReq;
import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.dto.UserDTO;
import com.agonylua.smartkitchen.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        User user = userService.register(req.getUsername(), req.getPassword(), req.getNickname());

        // Entity 转 DTO 返回
        return ApiResponse.success(UserDTO.fromEntity(user));
    }

    /**
     * 用户登录
     * 实际项目中应该校验密码加密，并生成 JWT Token
     */
    @PostMapping("/login")
    public ApiResponse<UserDTO> login(@RequestBody UserReq req) {
        UserDTO user = userService.login(req);
        log.info("获取的登录信息{}, token{}", req, user);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/updateUserInfo")
    public ApiResponse<Void> updateProfile(@RequestBody UserDTO req) {
        userService.update(req.getUserId(), req.getNickname(), req.getAvatarUrl());
        return ApiResponse.success(null);
    }
    /**
     * token 验证接口
     * 用于前端验证 token 是否有效
     */
    @PostMapping("/validateToken")
    public ApiResponse<Void> verifyToken() {
        return ApiResponse.success(null);
    }
}