package com.agonylua.smarthome.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DataConverter {

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) return "";
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null || value.isEmpty()) return null;
        return new Gson().fromJson(value, new TypeToken<List<String>>() {
        }.getType());
    }

    @TypeConverter
    public String fromMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    @TypeConverter
    public Map<String, String> toMap(String data) {
        if (data == null) {
            return null;
        }
        Gson gson = new Gson();
        // 这里需要使用 TypeToken 来保留泛型信息
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(data, type);
    }

    @TypeConverter
    public Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}