package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.model.UserRequest;
import com.agonylua.smarthome.repository.UserRepository;
import com.agonylua.smarthome.utils.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private MutableLiveData<Map<String, String>> usersDataList = new MutableLiveData<>();
    private static final String TAG = "UserViewModel";
    private MutableLiveData<Boolean> isLogin = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void loadUserData() {
        UserManager userManager = UserManager.getInstance(getApplication());
        Map<String, String> currentData = new HashMap<>();
        currentData.put("userId", userManager.getUserId());
        currentData.put("userName", userManager.getUserName());
        currentData.put("homeName", userManager.getNickName());
        currentData.put("nickName", userManager.getNickName());
        currentData.put("avatarUrl", userManager.getAvatarUrl());
        usersDataList.setValue(currentData);
        if (Objects.equals(currentData.get("userId"), "000000")) {
            Log.d(TAG, "loadUserData: " + "管理员账号");
        }
        isLogin.setValue(userManager.isLogIn());
    }

    public void logout() {
        UserManager userManager = UserManager.getInstance(getApplication());
        userManager.clear();
        isLogin.setValue(false);
        Toast.makeText(getApplication(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

    // 供 UI 调用
    public void saveProfile(String newNickname, String newHomeId, String newAvatarUrl) {
        // 构建请求对象
        UserRequest request = new UserRequest(newHomeId, newNickname, newAvatarUrl);

        repository.updateUserInfo(request, new UserRepository.UpdateCallback() {
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
}
