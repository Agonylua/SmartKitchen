package com.agonylua.smarthome.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class DataConverter {

    @TypeConverter
    public String fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    @TypeConverter
    public Map<String, Object> toMap(String data) {
        if (data == null) {
            return null;
        }
        Gson gson = new Gson();
        // 这里需要使用 TypeToken 来保留泛型信息
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(data, type);
    }
}