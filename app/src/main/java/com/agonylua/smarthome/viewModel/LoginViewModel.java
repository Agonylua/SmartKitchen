package com.agonylua.smarthome.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smarthome.repository.LoginRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;

public class LoginViewModel extends ViewModel {

    private LoginRepository repository;
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public void init(LoginRepository repository) {
        this.repository = repository;
    }

    // 供 Activity 调用的方法
    public void login(String username, String password) {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            repository.login(username, password, new LoginRepository.LoginCallback() {
                @Override
                public void onSuccess() {
                    loginResult.postValue(true);
                }

                @Override
                public void onError(String message) {
                    errorMessage.postValue(message);
                    loginResult.postValue(false);
                }

            });

        }, 5000);
    }

    public void tokenValidate() {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            repository.tokenValidate(new LoginRepository.ValidateCallback() {
                @Override
                public void onVerify(boolean isValid) {
                    loginResult.postValue(isValid);
                }

                @Override
                public void onFailure(String message) {
                    errorMessage.postValue(message);
                    loginResult.postValue(false);
                }
            });
        }, 5000);
    }

    public Boolean isNetwork() {
        return repository.validateNetwork();
    }

    public Boolean isExistToken() {
        return repository.isExistToken();
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