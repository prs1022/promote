package com.exception;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 华芳 on 2018/9/24.
 */
public class MyExceptionHandler {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public com.exception.Result handler(Exception e) {
        if (e instanceof MyException) {
            MyException myException = (MyException) e;
            return ResultUtil.error(myException.getCode(), myException.getMessage());
        } else {
            logger.error("[系统异常]:" + e.getMessage());
            e.printStackTrace();
            return ResultUtil.error(-1, "未知错误");
        }
    }
}
