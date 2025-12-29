package com.agonylua.smartkitchen.controller;

import com.agonylua.smartkitchen.databases.entity.Device;
import com.agonylua.smartkitchen.databases.repository.DeviceRepository;
import com.agonylua.smartkitchen.databases.repository.HomeRepository;
import com.agonylua.smartkitchen.databases.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller // 注意：这里是 @Controller，不是 @RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final HomeRepository homeRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 后台首页 / 数据看板
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 统计数据，用于页面顶部的数字卡片
        long userCount = userRepository.count();
        long homeCount = homeRepository.count();
        long deviceCount = deviceRepository.count();

        model.addAttribute("userCount", userCount);
        model.addAttribute("homeCount", homeCount);
        model.addAttribute("deviceCount", deviceCount);

        return "admin/dashboard"; // 对应 templates/admin/dashboard.html
    }

    /**
     * 设备管理列表
     */
    @GetMapping("/devices")
    public String deviceList(Model model) {
        List<Device> devices = deviceRepository.findAll();
        model.addAttribute("devices", devices);
        return "admin/deviceList";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login"; // 返回 login.html
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, HttpSession session) {
        if ("admin".equals(username) && "123456".equals(password)) {
            session.setAttribute("ADMIN_USER", username);
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login?error=1";
    }

    /**
     * 3. [新增] 仅返回设备表格的 HTML 片段 (用于 AJAX 局部刷新)
     * 语法: "模板名称 :: 片段名称(th:fragment)"
     */
    @GetMapping("/devices/fragment")
    public String deviceListFragment(Model model) {
        List<Device> devices = deviceRepository.findAll();
        model.addAttribute("devices", devices);

        return "admin/deviceList :: deviceTableBody";
    }

    /**
     * [新增] 仅返回首页统计数据的 JSON (如果你想让首页数字也跳动)
     * 这里演示用 @ResponseBody 返回 JSON，前端用 JS 更新数字
     */
    @GetMapping("/dashboard/data")
    @ResponseBody
    public Map<String, Long> getDashboardData() {
        Map<String, Long> data = new HashMap<>();
        data.put("userCount", userRepository.count());
        data.put("homeCount", homeRepository.count());
        data.put("deviceCount", deviceRepository.count());
        return data;
    }
}