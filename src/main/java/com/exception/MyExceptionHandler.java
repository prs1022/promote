package com.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by 华芳 on 2018/9/24.
 */
public class MyExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public com.exception.Result handler(Exception e) {
        if (e instanceof MyException) {
            MyException myException = (MyException) e;
            return ResultUtil.error(myException.getCode(), myException.getMessage());
        } else {
            System.err.println("[系统异常]:" + e.getMessage());
            e.printStackTrace();
            return ResultUtil.error(-1, "未知错误");
        }
    }
}
