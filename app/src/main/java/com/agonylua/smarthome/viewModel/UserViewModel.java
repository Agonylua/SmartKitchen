package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.adapter.HomeMemberAdapter;
import com.agonylua.smarthome.database.AppDatabase;
import com.agonylua.smarthome.database.entity.Home;
import com.agonylua.smarthome.dto.UserDTO;
import com.agonylua.smarthome.repository.UserRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.UserManager;

import java.io.File;
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
    private MutableLiveData<String> etNickname = new MutableLiveData<>();
    private MutableLiveData<String> etOldPwd = new MutableLiveData<>();
    private MutableLiveData<String> etNewPwd = new MutableLiveData<>();
    private MutableLiveData<String> etConfirmPwd = new MutableLiveData<>();
    private MutableLiveData<File> newAvatarFile = new MutableLiveData<>();
    private MediatorLiveData<Boolean> isSaveEnabled = new MediatorLiveData<>(false);
    private MutableLiveData<Boolean> isPasswordConsistency = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserInfoResult = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> refreshResult = new MutableLiveData<>();
    private MutableLiveData<ArrayList<HomeMemberAdapter.HomeMember>> memberList = new MutableLiveData<>();
    private LiveData<Home> home = new MutableLiveData<>();
    private UserRepository repository;
    private UserManager userManager;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = UserRepository.getInstance(application);
        userManager = UserManager.getInstance(getApplication());
        observeChanges();
    }

    public void observeChanges() {
        // 监听输入变化，控制保存按钮状态
        isSaveEnabled.addSource(etNickname, s -> isSaveEnabled.setValue(numericalChanges()));
        isSaveEnabled.addSource(etOldPwd, s -> isSaveEnabled.setValue(numericalChanges()));
        isSaveEnabled.addSource(etNewPwd, s -> isSaveEnabled.setValue(numericalChanges()));
        isSaveEnabled.addSource(etConfirmPwd, s -> isSaveEnabled.setValue(numericalChanges()));
        isSaveEnabled.addSource(newAvatarFile, s -> isSaveEnabled.setValue(numericalChanges()));
    }

    public Boolean numericalChanges() {
        String nickname = etNickname.getValue();
        String oldPwd = etOldPwd.getValue();
        String newPwd = etNewPwd.getValue();
        String confirmPwd = etConfirmPwd.getValue();

        boolean isNicknameChanged = nickname != null && !nickname.trim().isEmpty()
                && !nickname.equals(userManager.getNickName());

        boolean isAvatarChanged = newAvatarFile != null;
        boolean isOldPwdEmpty = oldPwd == null || oldPwd.trim().isEmpty();
        boolean isNewPwdEmpty = newPwd == null || newPwd.trim().isEmpty();
        boolean isConfirmPwdEmpty = confirmPwd == null || confirmPwd.trim().isEmpty();

        boolean isPasswordAllEmpty = isOldPwdEmpty && isNewPwdEmpty && isConfirmPwdEmpty;

        boolean isPasswordValid = !isOldPwdEmpty && !isNewPwdEmpty && !isConfirmPwdEmpty;
        if (!isPasswordAllEmpty && !isPasswordValid) {
            return false;
        }
        return isNicknameChanged || isAvatarChanged || isPasswordValid;
    }

    public void loadUserData() {
        Map<String, String> currentData = new HashMap<>();
        currentData.put("userId", userManager.getUserId());
        currentData.put("userName", userManager.getUserName());
        currentData.put("nickName", userManager.getNickName());
        currentData.put("avatarUrl", userManager.getAvatarUrl());
        currentData.put("homeId", userManager.getHomeId());
        Log.d(TAG, "loadUserData: 加载用户数据: " + currentData);
        usersDataList.setValue(currentData);
        isLogin.setValue(userManager.isLogIn());
    }

    public void refreshUserData() {
        repository.getUserInfo(new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                loadUserData();
                refreshResult.postValue(true);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "用户信息刷新失败: " + message, Toast.LENGTH_SHORT).show();
                refreshResult.postValue(false);
            }
        });
    }

    public void refreshHomeData(Home currentHome) {
        if (currentHome == null) return;
        btnExitHome.setValue(!currentHome.getOwnerId().equals(userManager.getUserId()));

        isOwner.setValue(currentHome.getOwnerId().equals(userManager.getUserId()));

        ArrayList<HomeMemberAdapter.HomeMember> list = new ArrayList<>();

        // 成员
        repository.getUserListInfo(new UserRepository.usersInfoCallback() {
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

    public void joinHome(String homeId) {
        repository.joinHome(homeId, new UserRepository.joinCallback() {
            @Override
            public void onSuccess(String refresh) {
                Toast.makeText(getApplication(), refresh, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "申请失败: " + message, Toast.LENGTH_SHORT).show();
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
        ThreadPoolUtils.getInstance().execute(() -> {
            AppDatabase.getInstance(getApplication()).homeDao().clearAll();
            AppDatabase.getInstance(getApplication()).deviceDao().clearAll();
            AppDatabase.getInstance(getApplication()).rulesDao().clearAll();
        });
        UserManager userManager = UserManager.getInstance(getApplication());
        userManager.clear();
        Toast.makeText(getApplication(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

    public void updateAvatar(File newAvatarFile) {
        Log.d(TAG, "updateAvatar: 准备更新头像，文件路径: " + newAvatarFile.getAbsolutePath());
        repository.updateUserAvatar(newAvatarFile, new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                loadUserData();
                isUserInfoResult.postValue(true);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "头像更新失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateNickname() {
        String newNickname = etNickname.getValue();
        repository.updateUserNickname(newNickname, new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                isUserInfoResult.postValue(true);
                loadUserData();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "昵称更新失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void resetPassword() {
        String oldPwd = etOldPwd.getValue();
        String newPwd = etNewPwd.getValue();
        repository.resetUserPassword(oldPwd, newPwd, new UserRepository.infoCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "密码更新成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplication(), "密码更新失败: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setAvatarFile(File file) {
        newAvatarFile.setValue(file);
    }

    public Boolean newPasswordChecks() {
        if (etOldPwd.getValue() == null && etNewPwd.getValue() == null && etConfirmPwd.getValue() == null) {
            return true;
        }
        String newPwd = etNewPwd.getValue();
        String confirmPwd = etConfirmPwd.getValue();
        return newPwd != null && newPwd.equals(confirmPwd);
    }

    public void saveProfile() {
        if (newAvatarFile.getValue() != null) {
            updateAvatar(newAvatarFile.getValue());
        }
        if (etNickname.getValue() != null && !etNickname.getValue().trim().isEmpty()
                && !etNickname.getValue().equals(userManager.getNickName())) {
            updateNickname();
        }
        if (etOldPwd.getValue() != null && !etOldPwd.getValue().trim().isEmpty()
                && etNewPwd.getValue() != null && !etNewPwd.getValue().trim().isEmpty()
                && etConfirmPwd.getValue() != null && !etConfirmPwd.getValue().trim().isEmpty()
                && etNewPwd.getValue().equals(etConfirmPwd.getValue())) {
            resetPassword();
        }
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

    public MutableLiveData<String> getEtNickname() {
        return etNickname;
    }

    public MutableLiveData<String> getEtOldPwd() {
        return etOldPwd;
    }

    public MutableLiveData<String> getEtNewPwd() {
        return etNewPwd;
    }

    public MutableLiveData<String> getEtConfirmPwd() {
        return etConfirmPwd;
    }

    public MediatorLiveData<Boolean> getIsSaveEnabled() {
        return isSaveEnabled;
    }

    public void setIsSaveEnabled(Boolean isSaveEnabled) {
        this.isSaveEnabled.setValue(isSaveEnabled);
    }

    public MutableLiveData<Boolean> getIsPasswordConsistency() {
        return isPasswordConsistency;
    }

    public MutableLiveData<File> getNewAvatarFile() {
        return newAvatarFile;
    }

    public MutableLiveData<Boolean> getIsUserInfoResult() {
        return isUserInfoResult;
    }

    public void setIsUserInfoResult(Boolean isUserInfoResult) {
        this.isUserInfoResult.setValue(isUserInfoResult);
    }
}
