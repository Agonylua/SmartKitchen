package com.agonylua.smarthome.ViewModel;

import androidx.lifecycle.ViewModel;

import com.agonylua.smarthome.Utils.TokenManager;

public class SetViewModel extends ViewModel {
    private TokenManager tokenManager;

    public SetViewModel() {
    }

    public void Cancellation() {
        tokenManager.clearToken();
    }
}
