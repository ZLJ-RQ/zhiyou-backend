package com.rq.zhiyou.common;

public enum StatusCode {
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    DATABASE_OPERATION_FAIL(40200,"数据库操作失败",""),
    NOT_FOUND_ERROR(40400, "请求数据不存在",""),
    SYSTEM_ERROR(50000,"系统内部异常",""),
    OPERATION_ERROR(50001, "操作失败","");

    private final int code;

    private final String msg;

    private final String description;

    StatusCode(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
