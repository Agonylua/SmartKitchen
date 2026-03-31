package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Home;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.dto.HomeDTO;
import com.agonylua.smartkitchen.utils.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private UserRepository userRepository;

    public void createHome(String userId) {
        Home home = new Home();
        String homeId = IdUtil.generateHomeId();
        List<String> memberIds = List.of(userId);
        userRepository.findByUserId(userId)
                .ifPresent(user -> {
                    home.setHomeId(homeId);
                    home.setHomeName(user.getNickname() + "的家");
                    home.setOwnerId(user.getUserId());
                    home.setMemberIds(memberIds);
                    homeRepository.save(home);
                    user.setHomeId(homeId);
                    homeRepository.save(home);
                });
    }

    public HomeDTO removeMember(String homeId, String userId) {
        return homeRepository.findByHomeId(homeId)
                .map(home -> {
                    List<String> memberIds = home.getMemberIds();
                    if (memberIds != null && memberIds.contains(userId)) {
                        // 移除成员ID
                        memberIds.remove(userId);
                        home.setMemberIds(memberIds);
                        homeRepository.save(home);
                        createHome(userId);
                        return HomeDTO.fromEntity(home);
                    }
                    return null; // 用户不在家庭成员中
                })
                .orElse(null); // 家庭不存在
    }

}
