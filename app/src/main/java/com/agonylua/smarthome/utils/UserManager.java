package com.agonylua.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class UserManager {
    private static final String PREF_NAME = "SmartKitchenApp";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_HOME_ID = "home_id";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR = "avatar_url";
    private static UserManager instance;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private static final String TAG = "UserManager";

    // 单例模式
    private UserManager(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    // 保存登录用户信息
    public void saveLoginInfo(String id, String homeId, String username, String nickname, String token) {
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_HOME_ID, homeId);
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_TOKEN, token);
        editor.apply(); // 异步提交
    }

    public void saveUser(String nickname, String avatarUrl, String homeId) {
        editor.putString(KEY_HOME_ID, homeId);
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_AVATAR, avatarUrl);
        editor.apply();
    }

    public Map<String, ?> getData() {
        return sp.getAll();
    }

    //-- Getter --//
    // 获取 Token
    public String getToken() {
        return sp.getString(KEY_TOKEN, "");
    }

    //-- Setter --//
    // 设置 Token
    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // 获取用户ID
    public String getUserId() {
        return sp.getString(KEY_USER_ID, "");
    }

    // 设置用户ID
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // 获取用户名
    public String getUserName() {
        return sp.getString(KEY_USERNAME, "");
    }

    // 获取家庭ID
    public String getHomeId() {
        return sp.getString(KEY_HOME_ID, "");
    }

    // 设置家庭ID
    public void setHomeId(String homeId) {
        editor.putString(KEY_HOME_ID, homeId);
        editor.apply();
    }

    // 获取头像URL
    public String getAvatarUrl() {
        return sp.getString(KEY_AVATAR, "");
    }

    // 设置头像URL
    public void setAvatarUrl(String url) {
        editor.putString(KEY_AVATAR, url);
        editor.apply();
    }

    // 获取昵称
    public String getNickName() {
        return sp.getString(KEY_NICKNAME, "未设置昵称");
    }

    // 设置用户名
    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    // 设置昵称
    public void setNickname(String nickname) {
        editor.putString(KEY_NICKNAME, nickname);
        editor.apply();
    }

    public boolean isLogIn() {
        return !getToken().isEmpty();
    }

    // 注销
    public void clear() {
        editor.clear();
        editor.apply();
    }
}