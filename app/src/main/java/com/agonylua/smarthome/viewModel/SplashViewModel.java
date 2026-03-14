package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.repository.HomeRepository;
import com.agonylua.smarthome.utils.NetworkMonitor;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.UserManager;

public class SplashViewModel extends AndroidViewModel {
    private final String TAG = "SplashViewModel";
    private HomeRepository repository;
    private MutableLiveData<Boolean> tokenValid = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        repository = new HomeRepository(getApplication());
    }

    public void LoginCheck() {
        ThreadPoolUtils.getInstance().executeDelay(this::validateToken, 2000);
    }

    /**
     * 验证Token有效性
     */
    public void validateToken() {
        UserManager userManager = UserManager.getInstance(getApplication());
        if (!userManager.isLogIn()) {
            tokenValid.postValue(false);
            return;
        }
        String token = userManager.getToken();
        Log.d(TAG, "validateToken: " + token);
        repository.validateToken(getApplication(), token, new HomeRepository.VerifyCallback() {
            @Override
            public void onVerify(Boolean valid) {
                if (valid) {
                    tokenValid.postValue(true);
                } else {
                    tokenValid.postValue(false);
                    userManager.clear();
                }
                Log.d(TAG, "Verify Token result: " + valid);
            }

            @Override
            public void onFailure(String errorMessage) {
                tokenValid.postValue(false);
            }
        });
    }

    public boolean validateNetwork() {
        return NetworkMonitor.getInstance(getApplication()).isInternetReachable();
    }

    public LiveData<Boolean> getTokenValid() {
        return tokenValid;
    }

}
