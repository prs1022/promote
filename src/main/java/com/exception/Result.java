package com.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * Created by 华芳 on 2018/9/24.
 */
@Setter
@Getter
public class Result {
    private int code;
    private String msg;
    private Object data;
}
