package com.rq.zhiyou.exception;

import com.rq.zhiyou.common.StatusCode;
import com.rq.zhiyou.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//切面注解
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获自定义异常
     * @param e
     * @return 通用返回对象,状态码是捕获自定义异常中的内容
     */
    @ExceptionHandler(BusinessException.class)
    public ResultData businessExceptionHandler(BusinessException e){
        return ResultData.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    /**
     * 捕获运行时异常
     * @param e
     * @return 通用返回对象,状态码是系统异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultData runtimeExceptionHandler(BusinessException e){
        return ResultData.error(StatusCode.SYSTEM_ERROR,e.getDescription());
    }
}
