package com.agonylua.smarthome.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.Repository.LoginRepository;
import com.agonylua.smarthome.Utils.TokenManager;

import org.jspecify.annotations.NonNull;

public class LoginViewModel extends AndroidViewModel {

    private LoginRepository repository;
    private TokenManager tokenManager;

    // LiveData 用于通知 Activity 更新 UI
    private MutableLiveData<String> loginSuccessToken = new MutableLiveData<>();
    private MutableLiveData<String> loginErrorMsg = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new LoginRepository();
    }

    // 供 Activity 调用的方法
    public void login(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorMsg.setValue("用户名或密码不能为空");
            return;
        }

        //isLoading.setValue(true); // 显示加载圈

        // 调用仓库，传入 LiveData 以便回调
        repository.login(getApplication(), username, password, new LoginRepository.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                loginSuccessToken.setValue(token);
            }

            @Override
            public void onError(String message) {
                loginErrorMsg.setValue(message);
            }

        });
    }

    public Boolean loginVerify() {
        String token = tokenManager.getToken();
        return token != null && !token.isEmpty();
    }

    // Getters for LiveData (供 Activity 观察)
    public LiveData<String> getLoginSuccessToken() {
        return loginSuccessToken;
    }

    public LiveData<String> getLoginErrorMsg() {
        return loginErrorMsg;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}