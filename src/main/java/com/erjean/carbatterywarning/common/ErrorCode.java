package com.erjean.carbatterywarning.common;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(200, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
