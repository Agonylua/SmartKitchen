package com.agonylua.smarthome.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}