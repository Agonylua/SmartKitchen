package com.agonylua.smartKitchen.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.agonylua.smartKitchen.repository.LoginRepository;
import com.agonylua.smartKitchen.utils.ThreadPoolUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private LoginRepository repository;
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<String> account = new MutableLiveData<>("");
    public final MutableLiveData<String> nickname = new MutableLiveData<>("");
    public final MutableLiveData<String> password = new MutableLiveData<>("");
    public final MutableLiveData<String> confirmPassword = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isNetwork = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>(false);

    @Inject
    public LoginViewModel(LoginRepository repository) {
        this.repository = repository;
    }

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
                    isConnected.postValue(false);
                }
            });
        }, 3000);
    }

    public void register(String reqUserName, String reqNickname, String reqPassword) {
        isLoading.setValue(true);
        errorMessage.setValue("");

        ThreadPoolUtils.getInstance().executeDelay(() -> {
            isLoading.postValue(false);
            repository.register(reqUserName, reqNickname, reqPassword, new LoginRepository.LoginCallback() {
                @Override
                public void onSuccess() {
                    registerSuccess.postValue(true);
                }

                @Override
                public void onError(String message) {
                    errorMessage.postValue(message);
                    registerSuccess.postValue(false);
                }
            });
        }, 3000);
    }

    public void retryConnection() {
        tokenValidate();
        loginResult.setValue(null);
        errorMessage.setValue(null);
        isNetwork.setValue(null);
        isConnected.setValue(true);
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void clearLoginResult() {
        loginResult.setValue(null);
    }

    // Getters for LiveData
    public MutableLiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public MutableLiveData<Boolean> getIsNetwork() {
        boolean networkStatus = repository.validateNetwork();
        isNetwork.postValue(networkStatus);
        return isNetwork;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }
}