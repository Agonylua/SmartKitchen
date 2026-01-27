package com.agonylua.smarthome.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agonylua.smarthome.repository.HomeRepository;
import com.agonylua.smarthome.utils.ThreadPoolUtils;
import com.agonylua.smarthome.utils.TokenManager;

public class SplashViewModel extends AndroidViewModel {
    private final String TAG = "SplashViewModel";
    private TokenManager tokenManager;
    private HomeRepository repository;
    private MutableLiveData<Integer> tokenValid = new MutableLiveData<>();

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
        tokenManager = new TokenManager(getApplication());
        Log.d(TAG, "Verify Token" + tokenManager.getToken());
        if (tokenManager.getToken() == null) {
            tokenValid.postValue(0);
            return;
        }
        String token = tokenManager.getToken();
        repository.validateToken(getApplication(), token, new HomeRepository.VerifyCallback() {
            @Override
            public void onVerify(Boolean valid) {
                if (valid) {
                    tokenValid.postValue(1);
                } else {
                    tokenValid.postValue(0);
                }
                Log.d(TAG, "Verify Token result: " + valid);
            }

            @Override
            public void onFailure(String errorMessage) {
                tokenValid.postValue(-1);
            }
        });
    }


    public LiveData<Integer> getTokenValid() {
        return tokenValid;
    }

}
