package com.agonylua.smartkitchen.handler;

import com.agonylua.smartkitchen.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 拦截我们手动抛出的 RuntimeException (如: "用户名已存在")
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getMessage()); // code: 500, msg: 错误信息
    }

    // 拦截所有未知的系统异常 (空指针等)
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error("系统繁忙，请稍后重试");
    }
}
