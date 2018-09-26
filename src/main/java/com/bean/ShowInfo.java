package com.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端展示的属性
 * Created by 华芳 on 2018/9/24.
 */
@Data
public class ShowInfo {
    private String realName;
    private String ip;
    private String inviteCode;
    private String inviteNum;
    private String moneyLeft;
    private long capacity;
    private double lordCount;
    private String refresh;
    private String partition;//lord分红
    private List<String> lordCollect = new ArrayList<>();
    private List<String> moneyCollect = new ArrayList<>();

    @Override
    public String toString() {
        return "当前用户:" + realName + ",IP:" + ip +
                ",邀请码:" + inviteCode + ",已邀请用户数:" + inviteNum +
                ",红包余额:" + moneyLeft + ",当前算力:" + capacity + ",当前lord数:" +
                lordCount;
    }
}
