package com.agonylua.smartKitchen.common;

import java.util.List;

public class DeviceResponse<T> {
    private int code;
    private String message;
    private List<T> data;

    public List<T> getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
