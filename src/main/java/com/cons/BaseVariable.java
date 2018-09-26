package com.cons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BaseVariable {
    public static final String baseUrl2 = "https://dapp2.kajsar.com";
    public static final String baseUrl = "https://dapp.kajsar.com";
    public static int tryTimes = 0;//重试登录次数
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static String token;
}
