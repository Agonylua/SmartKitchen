package com.agonylua.smarthome.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.agonylua.smarthome.Utils.TokenManager;

import org.jspecify.annotations.NonNull;

public class SetViewModel extends AndroidViewModel {
    private TokenManager tokenManager;

    public SetViewModel(@NonNull Application application) {
        super(application);
    }

    public void Cancellation() {
        tokenManager = new TokenManager(getApplication());
        tokenManager.clearToken();
    }
}
