package com.api.user;

import com.bean.LoginRs;
import com.bean.UserInfo;
import com.cons.BaseVariable;
import com.exception.ExceptionEnum;
import com.exception.MyException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.util.HttpUtils;
import lombok.Data;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Data
public class Login {


    private String phoneNum;

    private String pwd;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(Login.class);

    /**
     * 获取token
     *
     * @return
     */
    public String getToken() {
        String url = BaseVariable.baseUrl + "/api/auth/login";
        Map params = new HashMap();
        params.put("phone_num", phoneNum);
        params.put("password", pwd);
        params.put("jiguang_id", "");//极光推送ID,9月23号更新后已经去掉
        params.put("device_id", generateDeviceId());//新增设备号ID,如何生成 01AF7813-FF1F-4CD6-9693-2D3CC5C10081 或16位随机号（估计安卓） 可以用UUID取16位

        String response = HttpUtils.doPost(url, params, null);
        try {
            LoginRs res = new Gson().fromJson(response, new TypeToken<LoginRs>() {
            }.getType());
            return res.getData().get("token").toString();
        } catch (Exception e) {
            throw new MyException(ExceptionEnum.LOGIN_ERROR);
        }
    }

    public String generateDeviceId() {
        StringBuilder sb = new StringBuilder();
        for (char i : phoneNum.toCharArray()) {//已经有11位了
            sb.append((char) (i + 60));
        }
        //再+5位
        sb.append(phoneNum.charAt(0));
        sb.append(phoneNum.charAt(3));
        sb.append(phoneNum.charAt(5));
        sb.append(phoneNum.charAt(7));
        sb.append(phoneNum.charAt(9));
        logger.info("设备号:" + sb.toString());
        return sb.toString();
    }

    /**
     * 用户信息
     *
     * @param token
     * @return
     */
    public UserInfo userInfo(String token) {
        String url = BaseVariable.baseUrl + "/api/user/info";
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        String response = HttpUtils.doPost(url, new HashMap(), header);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(response, new TypeToken<UserInfo>() {
        }.getType());
    }

    /**
     * 获取已成功邀请的用户数
     *
     * @return
     */
    public String getInviteUserNum(String token) {
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        Map resultMap = new Gson().fromJson(HttpUtils.doPost(BaseVariable.baseUrl + "/api/user/friends", new HashMap(), header), new TypeToken<Map>() {
        }.getType());
        Map dataMap = new Gson().fromJson(new Gson().toJson(resultMap.get("data")), new TypeToken<Map>() {
        }.getType());
        return dataMap.get("friends_count").toString().replace(".0", "");
    }

    /**
     * 获取账户内红包金额
     *
     * @param token
     * @return
     */
    public String getMoney(String token) {
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        String response = HttpUtils.doPost(BaseVariable.baseUrl + "/api/envelope/user/info", new HashMap(), header);
        Map map = new Gson().fromJson(response, new TypeToken<Map>() {
        }.getType());
        Map dataMap = new Gson().fromJson(new Gson().toJson(map.get("data")), new TypeToken<Map>() {
        }.getType());
        return dataMap.get("credit").toString();
    }

}
