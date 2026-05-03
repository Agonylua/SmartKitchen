package com.agonylua.smartkitchen.service;

import com.agonylua.smartkitchen.databases.entity.Home;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import com.agonylua.smartkitchen.dto.HomeDTO;
import com.agonylua.smartkitchen.handler.WebSocketHandler;
import com.agonylua.smartkitchen.utils.IdUtil;
import com.agonylua.smartkitchen.utils.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class HomeService {
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebSocketConfig webSocketConfig;

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

    @Transactional
    public HomeDTO removeMember(String homeId, String memberId) {
        log.info("[家庭服务] 移除成员: homeId={}, memberId={}", homeId, memberId);
        return homeRepository.findByHomeId(homeId)
                .map(home -> {
                    List<String> memberIds = home.getMemberIds();
                    if (memberIds != null && memberIds.contains(memberId)) {
                        memberIds.remove(memberId);
                        home.setMemberIds(memberIds);
                        homeRepository.save(home);
                        createHome(memberId);

                        Map<String, String> applicantInfo = new HashMap<>();
                        applicantInfo.put("type", "REMOVE_REMIND");
                        applicantInfo.put("homeId", homeId);
                        String notificationJson = JsonUtil.toJson(applicantInfo);

                        WebSocketHandler.sendMessageToUser(memberId, notificationJson);

                        return HomeDTO.fromEntity(home);
                    }
                    return null; // 用户不在家庭成员中
                })
                .orElse(null); // 家庭不存在
    }

    public String joinHome(String homeId, String memberId) {
        log.info("[家庭服务] 收到申请: homeId={}, memberId={}", homeId, memberId);
        return homeRepository.findByHomeId(homeId)
                .map(home -> {
                    String mockOwnerId = home.getOwnerId();
                    if (Objects.equals(mockOwnerId, memberId)) return "你已经是户主了，无需申请加入";
                    if (home.getMemberIds().contains(memberId)) return "你已经是家庭成员了，无需申请加入";
                    Map<String, String> applicantInfo = new HashMap<>();
                    applicantInfo.put("type", "JOIN_REQUEST");
                    applicantInfo.put("memberId", memberId);
                    String notificationJson = JsonUtil.toJson(applicantInfo);

                    WebSocketHandler.sendMessageToUser(mockOwnerId, notificationJson);
                    return "申请已成功提交给服务器，正在通知户主审核";
                })
                .orElse("家庭不存在");
    }

    @Transactional
    public void joinHomeApproval(Boolean result, String ownerId, String memberId) {
        log.info("[家庭服务] 加入请求审批结果: result={}, ownerId={}, memberId={}", result, ownerId, memberId);
        if (result) {
            homeRepository.deleteByOwnerId(memberId);

            homeRepository.findByOwnerId(ownerId)
                    .ifPresent(home -> {
                        List<String> memberIds = home.getMemberIds() == null
                                ? new java.util.ArrayList<>()
                                : new java.util.ArrayList<>(home.getMemberIds());

                        if (!memberIds.contains(memberId)) {
                            memberIds.add(memberId);
                        }
                        home.setMemberIds(memberIds);

                        userRepository.findByUserId(memberId)
                                .ifPresent(user -> {
                                    user.setHomeId(home.getHomeId());
                                    userRepository.save(user);
                                });
                        homeRepository.save(home);
                    });
        } else {
            // TODO: (可选) 处理拒绝逻辑，如给 memberId 推送拒绝通知
            log.info("[家庭服务] 户主 {} 拒绝了用户 {} 的加入申请", ownerId, memberId);
        }
    }
}

