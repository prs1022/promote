package com.yima;

import com.api.user.Login;
import com.cons.BaseVariable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.util.HttpUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量实名认证
 * Created by 华芳 on 2018/9/25.
 */
public class RegisterBatch {
    private static final String direc = "2";

    public static String identity(String phoneNum, String pwd, String realName, String idCard) {
        System.out.println("开始认证;手机->" + phoneNum + ",姓名:" + realName + ",身份证:" + idCard);
        Login login = new Login();
        login.setPhoneNum(phoneNum);
        login.setPwd(pwd);
        String url = BaseVariable.baseUrl + "/api/user/identity";
        Map params = new HashMap();
        params.put("real_name", realName);
        params.put("id_card", idCard);
        Map header = new HashMap();
        header.put("authorization", "Bearer " + login.getToken());
        header.put("accept-language", "zh-cn");
        String response = HttpUtils.doPost(url, params, header);
        if (response == null) {
            return "跳过 phone:" + phoneNum + ",人名:" + realName + "的认证";
        }
        Map map = new Gson().fromJson(response, new TypeToken<Map>() {
        }.getType());
        String msg = "失败!!";
        if (map.get("code") != null && map.get("code") instanceof Number && Integer.parseInt(map.get("code").toString().replace(".0", "")) == 0) {
            msg = "绑定成功";
        } else if (map.get("data") != null) {
            msg = map.get("data").toString() + "_" + map.get("code");
        } else {
            System.out.println("未知错误！！！！" + map.get("code"));
        }
        return msg;
    }

    public static void main(String[] args) throws InterruptedException {
        File file = new File("src/main/resources/idcard/" + direc);
        File file2 = new File("src/main/resources/phone/" + direc);
        try {
            List<String> idcards = FileUtils.readLines(file);
            List<String> phones = FileUtils.readLines(file2);
            int length = idcards.size() < phones.size() ? idcards.size() : phones.size();
            for (int i = 0; i < length; i++) {
                System.out.println((i + 1) + ">>>>>>>>" + identity(phones.get(i).trim(), "123321", idcards.get(i).split(" ")[0].trim(), idcards.get(i).split(" ")[1].trim().substring(0, 18)));
                Thread.sleep(5000);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
