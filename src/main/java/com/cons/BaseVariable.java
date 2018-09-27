package com.cons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BaseVariable {
    public static final String baseUrl2 = "https://dapp2.kajsar.com";
    public static final String baseUrl = "https://dapp.kajsar.com";
    public static int tryTimes = 0;//重试登录次数
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static String token;

    public static Date getCurrentTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        c.get(Calendar.HOUR_OF_DAY);//24小时
        return c.getTime();
    }
}
