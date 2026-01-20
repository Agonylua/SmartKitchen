package com.agonylua.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private final String TAG = "TokenManager";
    private final UserManager userManager;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        userManager = UserManager.getInstance(context);
    }

    public void saveToken(String token) {
        userManager.setToken(token);
    }

    public String getToken() {
        return userManager.getToken();
    }
}
