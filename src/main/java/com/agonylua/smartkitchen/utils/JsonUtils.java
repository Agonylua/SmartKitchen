package com.agonylua.smartkitchen.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转 JSON 字符串 (存数据库用)
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    /**
     * JSON 字符串转 Map (给前端返回用)
     */
    public static Map<String, Object> parseMap(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return null;
        try {
            return mapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            return null;
        }
    }

    /**
     * 获取 JSON 中的某个特定字段值 (比如只拿 switch 状态)
     */
    public static String getValue(String jsonStr, String key) {
        try {
            JsonNode node = mapper.readTree(jsonStr);
            return node.has(key) ? node.get(key).asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}