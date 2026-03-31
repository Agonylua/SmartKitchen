package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.common.UserReq;
import com.agonylua.smartkitchen.databases.entity.Home;
import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.dto.UserDTO;
import com.agonylua.smartkitchen.utils.IdUtil;
import com.agonylua.smartkitchen.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Transactional // 事务管理：用户和家庭必须同时创建成功
    public User register(String username, String password, String inputNickname) {

        // 1. 校验用户名 (只能包含字母和数字)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母和数字");
        }

        // 2. 准备用户对象
        User user = new User();
        // 生成唯一ID (实际生产中需检查ID是否碰撞，这里简化)
        user.setUserId(IdUtil.generateUserId());
        user.setUsername(username);
        user.setPassword(password); // 记得加密！

        // 3. 处理默认昵称逻辑
        if (inputNickname == null || inputNickname.trim().isEmpty()) {
            user.setNickname("用户" + username); // 默认昵称为用户名
        } else {
            user.setNickname(inputNickname);
        }

        // 4. 保存用户
        userRepository.save(user);

        // 5. 自动创建家庭
        Home home = new Home();
        home.setHomeId(IdUtil.generateHomeId()); // 生成6位家庭ID
        home.setOwnerId(user.getUserId());
        // 默认家庭名称: 昵称 + 的家
        home.setHomeName(user.getNickname() + "的家");

        // 初始成员列表为空或包含户主自己
        home.getMemberIds().add(user.getUserId());

        homeRepository.save(home);

        return user;
    }

    public UserDTO login(UserReq req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Home home = homeRepository.findByOwnerId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("家庭不存在"));
        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(req.getUsername(), user.getUserId());
        UserDTO dto = UserDTO.fromEntity(user);
        dto.setHomeId(home.getHomeId());
        dto.setToken(token);
        return dto;
    }

    public void update(String id, String nickname, String avatarUrl) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    public List<UserDTO> findAllByHomeId(String homeId) {
        List<UserDTO> dto = new ArrayList<>();
        List<User> users = userRepository.findByHomeId(homeId);
        for (User user : users) {
            UserDTO userDTO = UserDTO.fromEntity(user);
            dto.add(userDTO);
        }
        return dto;
    }

    public UserDTO exitHome(String homeId, String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> {
                    if (user.getHomeId().equals(homeId)) return UserDTO.fromEntity(user);
                    String newHome = IdUtil.generateHomeId();
                    user.setHomeId(newHome);
                    userRepository.save(user);
                    return UserDTO.fromEntity(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("未找到相关家庭信息"));
    }

}