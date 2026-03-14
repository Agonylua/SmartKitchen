package com.agonylua.smarthome.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.repository.LoginRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

import org.jspecify.annotations.NonNull;

public class LoginViewModel extends AndroidViewModel {

    private LoginRepository repository;
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new LoginRepository();
    }

    // 供 Activity 调用的方法
    public void login(String username, String password) {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            repository.login(getApplication(), username, password, new LoginRepository.LoginCallback() {
                @Override
                public void onSuccess(String token) {
                    loginResult.postValue(true);
                }

                @Override
                public void onError(String message) {
                    errorMessage.postValue(message);
                }

            });

        }, 2000);
    }


    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    // Getters for LiveData
    public MutableLiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}