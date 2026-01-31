package com.agonylua.smarthome.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /**
     * 将任意对象转换为 JSON 字符串
     * 场景：保存数据到数据库前，先转成 String
     */
    public static String toJson(Object object) {
        if (object == null) {
            return "";
        }
        return gson.toJson(object);
    }

    /**
     * 将 JSON 字符串转换为 Map<String, Object>
     * 场景：从数据库读取 String 后，转为 Map 方便取值
     */
    public static Map<String, Object> toMap(String json) {
        if (TextUtils.isEmpty(json)) {
            return new HashMap<>();
        }
        try {
            // 使用 TypeToken 处理泛型擦除
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>(); // 解析失败返回空 Map，防止 Crash
        }
    }

    /**
     * 将 JSON 字符串转换为指定对象实体
     */
    public static <T> T toObject(String json, Class<T> cls) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return gson.fromJson(json, cls);
        } catch (Exception e) {
            return null;
        }
    }

    private static void setValueSafely(Map<String, String> map, String key, String value) {
        if (map != null && key != null && value != null) {
            map.put(key, value);
        }
    }

}