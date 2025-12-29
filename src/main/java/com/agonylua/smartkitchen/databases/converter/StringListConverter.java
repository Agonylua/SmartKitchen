package com.agonylua.smartkitchen.databases.converter;

import com.agonylua.smartkitchen.utils.JsonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return JsonUtil.toJson(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return JsonUtil.parseList(dbData, String.class);
    }
}
