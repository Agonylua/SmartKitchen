package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.User;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // 注入你的数据库接口

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 返回 Spring Security 需要的 UserDetails 对象
        // 这里只是简单示例，没有放权限（Authorities），实际项目中可能需要放 "ROLE_USER" 等
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 这里的密码应该是数据库里加密后的密码
                new ArrayList<>() // 权限列表，暂时为空
        );
    }
}