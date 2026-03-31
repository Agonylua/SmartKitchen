package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.adapter.HomeMemberAdapter;
import com.agonylua.smarthome.common.UserRequest;
import com.agonylua.smarthome.database.entity.Home;
import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.repository.UserRepository;
import com.agonylua.smarthome.utils.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private MutableLiveData<Map<String, String>> usersDataList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLogin = new MutableLiveData<>();
    private MutableLiveData<Boolean> isOwner = new MutableLiveData<>();
    private MutableLiveData<Boolean> btnExitHome = new MutableLiveData<>();
    private MutableLiveData<ArrayList<HomeMemberAdapter.HomeMember>> memberList = new MutableLiveData<>();
    private LiveData<Home> home = new MutableLiveData<>();
    private UserRepository repository;
    private UserManager userManager;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        userManager = UserManager.getInstance(getApplication());
    }

    public void loadUserData() {
        Map<String, String> currentData = new HashMap<>();
        currentData.put("userId", userManager.getUserId());
        currentData.put("userName", userManager.getUserName());
        currentData.put("nickName", userManager.getNickName());
        currentData.put("avatarUrl", userManager.getAvatarUrl());
        Log.d(TAG, "loadUserData: 加载用户数据: " + currentData);
        usersDataList.setValue(currentData);
        isLogin.setValue(userManager.isLogIn());
    }

    public void refreshHomeData(Home currentHome) {
        if (currentHome == null) return;
        btnExitHome.setValue(!currentHome.getOwnerId().equals(userManager.getUserId()));

        isOwner.setValue(currentHome.getOwnerId().equals(userManager.getUserId()));

        ArrayList<HomeMemberAdapter.HomeMember> list = new ArrayList<>();

        // 成员
        repository.getUsersInfo(new UserRepository.usersInfoCallback() {
            @Override
            public void onSuccess(List<UserDTO> users) {
                Log.d(TAG, "onSuccess: 获取用户信息成功: " + users.toString());
                for (UserDTO user : users) {
                    String userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                    if (user.getUserId().equals(currentHome.getOwnerId())) {
                        // 户主
                        list.add(new HomeMemberAdapter.HomeMember(
                                user.getUserId(),
                                userName,
                                user.getAvatarUrl(),
                                true,
                                user.getUserId().equals(userManager.getUserId())));
                    } else {
                        // 普通成员
                        list.add(new HomeMemberAdapter.HomeMember(
                                user.getUserId(),
                                userName,
                                user.getAvatarUrl(),
                                false,
                                user.getUserId().equals(userManager.getUserId())));
                    }
                }
                // 户主置顶排序
                Collections.sort(list, (o1, o2) -> {
                    if (o1.isOwner) return -1;
                    if (o2.isOwner) return 1;
                    return 0;
                });
                memberList.setValue(list);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "getMemberList: 获取用户信息失败: " + message);
            }
        });
    }

    public void removeMember(String memberId) {
        repository.removeMember(memberId, new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "成员移除成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "成员移除失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void exitHome() {
        repository.exitHome(new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "已退出家庭", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "退出家庭失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void logout() {
        UserManager userManager = UserManager.getInstance(getApplication());
        userManager.clear();
        isLogin.setValue(false);
        Toast.makeText(getApplication(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

    public void saveProfile(String newNickname, String newHomeId, String newAvatarUrl) {
        // 构建请求对象
        UserRequest request = new UserRequest(newHomeId, newNickname, newAvatarUrl);

        repository.updateUserInfo(request, new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                // TODO 更新相应的 LiveData
                Toast.makeText(getApplication(), "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "保存失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //-- Getters for LiveData --//
    public LiveData<Map<String, String>> getUsersDataList() {
        return usersDataList;
    }

    public MutableLiveData<Boolean> getIsLogin() {
        return isLogin;
    }

    public MutableLiveData<Boolean> getBtnExitHome() {
        return btnExitHome;
    }

    public LiveData<Home> getHome() {
        home = repository.getHome();
        return home;
    }

    public MutableLiveData<Boolean> getIsOwner() {
        return isOwner;
    }

    public MutableLiveData<ArrayList<HomeMemberAdapter.HomeMember>> getMemberList() {
        return memberList;
    }
}
