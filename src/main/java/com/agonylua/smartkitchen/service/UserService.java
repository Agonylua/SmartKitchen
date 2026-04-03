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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

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
        Home home = homeRepository.findByMemberId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("家庭不存在"));
        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(req.getUsername(), user.getUserId());
        UserDTO dto = UserDTO.fromEntity(user);
        dto.setHomeId(home.getHomeId());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setToken(token);
        return dto;
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

    public String uploadAvatarFile(String userId, MultipartFile file) {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            log.error("上传文件失败，文件为空");
        }

        try {
            // 2. 获取原始文件名并提取后缀 (例如: .jpg, .png)
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. 生成全局唯一的崭新文件名，防止不同用户上传同名文件被覆盖
            String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 4. 确保物理存储目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 递归创建目录
            }

            // 5. 将内存中的文件流写入到磁盘的目标文件中
            File destFile = new File(directory.getAbsolutePath(), newFileName);
            file.transferTo(destFile);

            // 6. 拼接文件的网络访问 URL 并返回给客户端
            String fileUrl = accessUrl + newFileName;

            // 返回成功响应及图片的 URL
            userRepository.findByUserId(userId).ifPresent(user -> {
                user.setAvatarUrl(fileUrl);
                userRepository.save(user);
            });
            return fileUrl;

        } catch (Exception e) {
            // 7. 捕获并处理可能发生的异常，例如文件写入失败等
            log.error("上传文件时发生异常", e);
            return null; // 或者抛出自定义异常，视业务需求而定
        }
    }

}