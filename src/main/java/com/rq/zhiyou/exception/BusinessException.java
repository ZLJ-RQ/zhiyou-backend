package com.rq.zhiyou.exception;

import com.rq.zhiyou.common.StatusCode;

public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(StatusCode statusCode) {
        super(statusCode.getMsg());
        this.code = statusCode.getCode();
        this.description = statusCode.getDescription();
    }

    public BusinessException(StatusCode statusCode, String description) {
        super(statusCode.getMsg());
        this.code = statusCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
