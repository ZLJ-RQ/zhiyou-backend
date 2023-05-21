package com.rq.zhiyou.utils;

import com.rq.zhiyou.common.StatusCode;
import lombok.Data;


@Data
public class ResultData<T> {

    private int code;

    private T data;

    private String msg;

    private String description;

    public ResultData(int code, T data, String msg, String description) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.description = description;
    }

    public ResultData(StatusCode statusCode,T data) {
        this(statusCode.getCode(),data,statusCode.getMsg(),statusCode.getDescription());
    }

    public ResultData(StatusCode statusCode) {
        this(statusCode.getCode(),null,statusCode.getMsg(),statusCode.getDescription());
    }

    public ResultData(StatusCode statusCode,String description) {
        this(statusCode.getCode(),null,statusCode.getMsg(),description);
    }

    /**
     * 操作成功，且有数据返回时
     */
    public static<T> ResultData success(T data){
        return new ResultData(StatusCode.SUCCESS,data);
    }

    /**
     * 操作成功，且没有数据返回时
     */
    public static<T> ResultData success(){
        return new ResultData(StatusCode.SUCCESS);
    }

    /**
     * 操作失败,Error
     */
    public static<T> ResultData error(StatusCode statusCode,String description){
        return new ResultData(statusCode,description);
    }

    public static<T> ResultData error(int code,String msg, String description){
        return new ResultData(code,null,msg,description);
    }
}
