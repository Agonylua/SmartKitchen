package com.agonylua.smarthome.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.Repository.HomeRepository;
import com.agonylua.smarthome.Utils.NetworkUtils;
import com.agonylua.smarthome.Utils.ThreadPoolUtils;
import com.agonylua.smarthome.Utils.TokenManager;

public class SplashViewModel extends AndroidViewModel {
    private final String TAG = "SplashViewModel";
    private TokenManager tokenManager;
    private NetworkUtils networkUtils;
    private HomeRepository repository;
    private MutableLiveData<Boolean> tokenValid = new MutableLiveData<>();
    private MutableLiveData<Boolean> hasNetwork = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository();
    }

    public void LoginCheck() {
        ThreadPoolUtils.getInstance().executeDelay(() -> {
            HasNetwork();
            validateToken();
        }, 1000);
    }

    /**
     * 验证Token有效性
     */
    public void validateToken() {
        tokenManager = new TokenManager(getApplication());
        Log.d(TAG, "validateToken: 开始验证 Token" + tokenManager.getToken());
        if (tokenManager.getToken() == null) {
            Log.d(TAG, "validateToken: Token is null");
            tokenValid.postValue(false);
            return;
        }
        String token = tokenManager.getToken();
        repository.validateToken(getApplication(), token, new HomeRepository.VerifyCallback() {
            @Override
            public void onVerify(Boolean valid) {

                tokenValid.postValue(valid);
                Log.d(TAG, "onVerify: Token 有效性验证结果: " + valid);
            }
        });
    }

    public void HasNetwork() {
        boolean networkStatus = NetworkUtils.isNetworkAvailable(getApplication());
        hasNetwork.postValue(networkStatus);
    }

    public void LoginStatus(Boolean loading) {
        isLoading.postValue(loading);
    }

    public LiveData<Boolean> getTokenValid() {
        return tokenValid;
    }

    public LiveData<Boolean> getHasNetwork() {
        return hasNetwork;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
