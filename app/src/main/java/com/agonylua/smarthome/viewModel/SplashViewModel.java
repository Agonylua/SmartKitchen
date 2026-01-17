package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.repository.HomeRepository;
import com.agonylua.smarthome.utils.NetworkUtils;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.TokenManager;

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
        repository = new HomeRepository(getApplication());
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
        Log.d(TAG, "Verify Token" + tokenManager.getToken());
        if (tokenManager.getToken() == null) {
            tokenValid.postValue(false);
            return;
        }
        String token = tokenManager.getToken();
        repository.validateToken(getApplication(), token, new HomeRepository.VerifyCallback() {
            @Override
            public void onVerify(Boolean valid) {

                tokenValid.postValue(valid);
                Log.d(TAG, "Verify Token result: " + valid);
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
