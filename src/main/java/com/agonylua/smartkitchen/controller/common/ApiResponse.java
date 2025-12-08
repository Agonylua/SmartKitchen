package com.agonylua.smartkitchen.controller.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private Integer code; // 200:成功, 500:错误
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 500;
        r.message = msg;
        return r;
    }
}