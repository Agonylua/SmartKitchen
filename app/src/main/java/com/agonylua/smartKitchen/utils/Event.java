package com.agonylua.smartkitchen.utils;

/**
 * 用于包装通过 LiveData 发送的一次性事件 (如 Toast, Snackbar, 页面跳转)
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * 获取内容并标记为已处理。如果已处理过，则返回 null。
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * 无论是否被处理过，都返回内容 (极少情况使用)
     */
    public T peekContent() {
        return content;
    }
}