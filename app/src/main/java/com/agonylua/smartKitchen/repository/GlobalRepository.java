package com.agonylua.smartKitchen.repository;

import com.agonylua.smartKitchen.common.ApiResponse;
import com.agonylua.smartKitchen.database.dao.DeviceDao;
import com.agonylua.smartKitchen.database.dao.HomeDao;
import com.agonylua.smartKitchen.database.dao.RulesDao;
import com.agonylua.smartKitchen.database.entity.Device;
import com.agonylua.smartKitchen.database.entity.Home;
import com.agonylua.smartKitchen.database.entity.Rules;
import com.agonylua.smartKitchen.dto.AutomationRuleDTO;
import com.agonylua.smartKitchen.dto.UserDTO;
import com.agonylua.smartKitchen.network.RetrofitClient;
import com.agonylua.smartKitchen.utils.RuleMapper;
import com.agonylua.smartKitchen.utils.UserManager;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GlobalRepository {
    private final RetrofitClient retrofit;
    private final UserManager userManager;
    private final DeviceDao deviceDao;
    private final HomeDao homeDao;
    private final RulesDao rulesDao;

    @Inject
    public GlobalRepository(
            RetrofitClient retrofit,
            UserManager userManager,
            DeviceDao deviceDao,
            HomeDao homeDao,
            RulesDao rulesDao) {
        this.homeDao = homeDao;
        this.rulesDao = rulesDao;
        this.deviceDao = deviceDao;
        this.retrofit = retrofit;
        this.userManager = userManager;
    }

    public void syncDeviceState() {
        try {
            ApiResponse<List<Device>> apiResponse = retrofit.getApi().getDeviceList(userManager.getHomeId()).execute().body();
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<Device> deviceList = apiResponse.getData();
                deviceDao.clearAll();
                deviceDao.insertAll(deviceList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncHomeInfo() {
        try {
            ApiResponse<Home> apiResponse = retrofit.getApi().getHomeInfo(userManager.getHomeId()).execute().body();
            if (apiResponse != null && apiResponse.getCode() == 200) {
                Home home = apiResponse.getData();
                homeDao.clearAll();
                homeDao.insertAll(home);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncUserInfo() {
        try {
            ApiResponse<UserDTO> apiResponse = retrofit.getApi().getUserInfo().execute().body();
            if (apiResponse != null && apiResponse.getCode() == 200) {
                userManager.saveUser(
                        apiResponse.getData().getNickname(),
                        apiResponse.getData().getAvatarUrl()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncAutomationRules() {
        try {
            ApiResponse<List<AutomationRuleDTO>> apiResponse = retrofit.getApi().getRules().execute().body();
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<AutomationRuleDTO> ruleDTOs = apiResponse.getData();
                List<Rules> rules = RuleMapper.toRulesList(ruleDTOs, userManager.getUserId());
                rulesDao.clearAll();
                rulesDao.insertAll(rules);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void syncDevicePower() {
//        try {
//            ApiResponse<List<DevicePowerDTO>> apiResponse = retrofit.getApi().getDevicesPower(userManager.getHomeId()).execute().body();
//            if (apiResponse != null && apiResponse.getCode() == 200) {
//                List<DevicePowerDTO> devicePowerList = apiResponse.getData();
//                monitorViewModel.setPowerData(devicePowerList);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void syncAllData() {
        syncDeviceState();
        syncHomeInfo();
        syncUserInfo();
        syncAutomationRules();
//        syncDevicePower();
    }
}