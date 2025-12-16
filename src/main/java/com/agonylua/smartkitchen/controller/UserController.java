package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.common.ApiResponse;
import com.agonylua.smartkitchen.common.RegisterReq;
import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.dto.UserDTO;
import com.agonylua.smartkitchen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    public ApiResponse<UserDTO> login(@RequestBody RegisterReq req) {
        User user = userService.login(req.getUsername(), req.getPassword());

        // JWT Token
        UserDTO dto = UserDTO.fromEntity(user);
        dto.setToken("mock-jwt-token-" + user.getUserId());

        return ApiResponse.success(dto);
    }
}