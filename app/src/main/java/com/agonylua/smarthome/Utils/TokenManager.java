package com.agonylua.smarthome.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "SmartKitchenApp";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
