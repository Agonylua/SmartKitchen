package com.agonylua.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {
    private final String TAG = "TokenManager";
    private final UserManager userManager;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        userManager = UserManager.getInstance(context);
    }

    public void saveToken(String token) {
        Log.d(TAG, "Saving token: " + token);
        userManager.setToken(token);
    }

    public String getToken() {
        Log.d(TAG, "Getting token: " + userManager.getToken());
        return userManager.getToken();
    }
}
