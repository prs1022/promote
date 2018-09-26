package com.bean;

import lombok.Data;

import java.util.Map;

@Data
public class LoginRs {
    private int status;
    private int code;
    private Map data;//key->token,val->token值
}
