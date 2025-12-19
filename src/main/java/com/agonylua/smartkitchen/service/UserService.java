package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Home;
import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.utils.IdUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HomeRepository homeRepository;

    @Transactional // 事务管理：用户和家庭必须同时创建成功
    public User register(String username, String password, String inputNickname) {

        // 1. 校验用户名 (只能包含字母和数字)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母和数字");
        }

        // 2. 准备用户对象
        User user = new User();
        // 生成唯一ID (实际生产中需检查ID是否碰撞，这里简化)
        user.setUserId(IdUtils.generateShortId());
        user.setUsername(username);
        user.setPassword(password); // 记得加密！

        // 3. 处理默认昵称逻辑
        if (inputNickname == null || inputNickname.trim().isEmpty()) {
            user.setNickname(username); // 默认昵称为用户名
        } else {
            user.setNickname(inputNickname);
        }

        // 4. 保存用户
        userRepository.save(user);

        // 5. 自动创建家庭
        Home home = new Home();
        home.setHomeId(IdUtils.generateShortId()); // 生成6位家庭ID
        home.setOwnerId(user.getUserId());
        // 默认家庭名称: 昵称 + 的家
        home.setHomeName(user.getNickname() + "的家");

        // 初始成员列表为空或包含户主自己
        home.getMemberIds().add(user.getUserId());

        homeRepository.save(home);

        return user;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }
}