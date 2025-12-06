package com.agonylua.smartkitchen.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Json 工具类
 *
 * @author liuwei
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 外层关键字段
    private static final String KEY = "R";
    private static final String KEY_SN = "SN";
    private static final String KEY_NAME = "Name";
    private static final String KEY_STATUS = "Status";
    private static final String KEY_DATA = "Data";

    private JsonUtil() {
    }

    /* ==================== 基础转换 ==================== */

    /**
     * 对象 → JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败", e);
            return null;
        }
    }

    // JSON 字符串 → JsonNode
    public static JsonNode parse(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("解析JSON失败: {}", json, e);
            return null;
        }
    }

    /* ==================== 外层字段读取 ==================== */

    // 获取事件编号 R
    public static String getR(JsonNode root) {
        return getString(root, KEY);
    }

    // 获取设备序列号 SN
    public static String getSN(JsonNode root) {
        return getString(root, KEY_SN);
    }

    // 获取设备名称 Name
    public static String getName(JsonNode root) {
        return getString(root, KEY_NAME);
    }

    // 获取设备状态 Status
    public static String getStatus(JsonNode root) {
        return getString(root, KEY_STATUS);
    }

    // 获取 Data 字段
    public static String getDataAsString(JsonNode root) {
        return getString(root, KEY_DATA);
    }

    public static JsonNode getDataAsJsonNode(JsonNode root) {
        String dataStr = getString(root, KEY_DATA);
        if (!StringUtils.hasText(dataStr)) {
            return null;
        }
        try {
            return MAPPER.readTree(dataStr);
        } catch (JsonProcessingException e) {
            log.warn("Data 字段不是合法JSON: {}", dataStr);
            return null;
        }
    }

    /* ==================== 外层字段修改 ==================== */

    /**
     * 设置 R
     */
    public static void setR(JsonNode root, String r) {
        setString(root, KEY, r);
    }

    /**
     * 设置 SN
     */
    public static void setSN(JsonNode root, String sn) {
        setString(root, KEY_SN, sn);
    }

    /**
     * 设置 Name
     */
    public static void setName(JsonNode root, String name) {
        setString(root, KEY_NAME, name);
    }

    /**
     * 设置 Status
     */
    public static void setStatus(JsonNode root, String status) {
        setString(root, KEY_STATUS, status);
    }

    /**
     * 替换整个 Data 字段（传入任意对象，会自动转成 JSON 字符串）
     */
    public static void setData(JsonNode root, Object dataObj) {
        if (dataObj == null) {
            setString(root, KEY_DATA, null);
            return;
        }
        String jsonStr = toJson(dataObj);
        setString(root, KEY_DATA, jsonStr);
    }

    /* ==================== 通用辅助方法 ==================== */

    private static String getString(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode n = node.get(fieldName);
        return n.isNull() ? null : n.asText();
    }

    private static void setString(JsonNode node, String fieldName, String value) {
        if (!(node instanceof ObjectNode objectNode)) {
            throw new IllegalArgumentException("node 必须是 ObjectNode 才能修改");
        }
        if (StringUtils.hasText(value)) {
            objectNode.put(fieldName, value);
        } else {
            objectNode.remove(fieldName);
        }
    }

}