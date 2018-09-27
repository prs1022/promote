package com.exception;

import lombok.Getter;

/**
 * Created by 华芳 on 2018/9/24.
 */

@Getter
public enum ExceptionEnum {
    UNKONW_ERROR(-1, "未知错误"), SUCCESS(0, "成功"),

    LOGIN_ERROR(100, "用户名密码错误"), TIME_OUT(501, "服务端错误"), SESSION_TIME_OUT(101, "登录超时"),
    THREE_TRY_OUT(101, "三次重试登录次数用完");
    private Integer code;
    private String msg;

    ExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
