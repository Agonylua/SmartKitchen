package com.agonylua.smartkitchen.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 核心方法：对象转 JSON 字符串 (存数据库用)
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;

        if (obj instanceof String) {
            return (String) obj;
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * Map 转 JSON 字符串
     */
    public static String mapToJson(Map<?, ?> map) {
        return toJson(map);
    }

    /**
     * JSON 字符串转 Map (给前端返回用)
     */
    public static Map<String, Object> parseMap(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return null;
        try {
            return mapper.readValue(jsonStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", jsonStr, e);
            return null;
        }
    }

    /**
     * 获取 JSON 中的某个特定字段值
     */
    public static String getValue(String jsonStr, String key) {
        if (jsonStr == null || jsonStr.isEmpty()) return null;
        try {
            JsonNode node = mapper.readTree(jsonStr);
            return node.has(key) ? node.get(key).asText() : null;
        } catch (Exception e) {
            log.warn("解析JSON字段失败 Key: {}, JSON: {}", key, jsonStr);
            return null;
        }
    }

    /**
     * 解析 List
     */
    public static <T> List<T> parseList(String jsonStr, Class<T> clazz) {
        if (jsonStr == null || jsonStr.isEmpty()) return new ArrayList<>();
        try {
            // 构建 List<T> 的类型
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return mapper.readValue(jsonStr, listType);
        } catch (Exception e) {
            log.error("List反序列化失败", e);
            return new ArrayList<>();
        }
    }
}