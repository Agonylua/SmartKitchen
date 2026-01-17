package com.agonylua.smarthome.viewModel;

import android.app.Application;
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

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private MutableLiveData<Map<String, String>> usersDataList = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void loadUserData() {
        UserManager userManager = UserManager.getInstance(getApplication());
        Map<String, String> currentData = new HashMap<>();
        currentData.put("userName", userManager.getUserName());
        currentData.put("homeName", userManager.getNickName() + "的家");
        currentData.put("nickname", userManager.getNickName());
        currentData.put("avatarUrl", userManager.getAvatarUrl());
        usersDataList.setValue(currentData);
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
}
