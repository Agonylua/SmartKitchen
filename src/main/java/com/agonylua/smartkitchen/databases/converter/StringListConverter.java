package com.agonylua.smartkitchen.databases.converter;

import com.agonylua.smartkitchen.utils.JsonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    // 将 List<String> 转换为 JSON 字符串存储到数据库中
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return JsonUtil.toJson(attribute);
    }

    // 将数据库中的 JSON 字符串转换回 List<String>
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return JsonUtil.parseList(dbData, String.class);
    }
}
