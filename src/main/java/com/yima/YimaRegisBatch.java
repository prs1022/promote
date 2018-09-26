package com.yima;

import com.cons.BaseVariable;
import com.util.HttpUtils;
import com.util.MD5;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 批量注册
 */
public class YimaRegisBatch {

    private String token = "0036533809e59286c9d84026120ffed788134de6";//长期有效，不换密码的前提下


    public String getPhoneNum() {//23725 凯撒黑卡的项目编号
        String url = "http://api.fxhyd.cn/UserInterface.aspx?action=getmobile&token=" + token + "&itemid=23725";
        String res = HttpUtils.doGet(url, new HashMap());//success|手机号码
        if (res.split("|").length <= 1) {
            throw new RuntimeException("获取手机号失败！！！");
        }
        return res.split("|")[1];
    }

    public String getShortCode(String phoneNum) {
        int i = 0;//调用的间隔
        while (i <= 80) {//超过60秒成功率会高点
            String url = "http://api.fxhyd.cn/UserInterface.aspx?action=getsms&token=TOKEN&itemid=23725&mobile=" + phoneNum + "&release=1";
            String response = HttpUtils.doGet(url, new HashMap());//success|短信内容
            System.out.println("第" + (i / 5 + 1) + "次获取短信....");
            if (response.equals("3001")) {
                //短信尚未到达，继续调用
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (response.split("|").length <= 1) {//其他错误代码
                System.err.println("错误代码：" + response);
                throw new RuntimeException("获取短信失败！！！");
            } else {
                return response.split("|")[1];
            }
            i += 5;//每隔五秒执行一次获取短信
        }
        return null;
    }

    public void register() {
        String phoneNum = getPhoneNum();
        invoke(phoneNum);
        String code = getShortCode(phoneNum);
        System.out.println("获取到的手机号为" + phoneNum + ",短信验证码为:" + code);
    }

    private void invoke(String phoneNum) {
        // {"phone_num":"17164969554","time":1537386419,"sign":"446aa533ee26c043d3baf36802cb514e"}
        // {"phone_num":"17164969554","time":1537386434,"sign":"1cfa937924a45ef86f9a1e92a82d3a24"}
        // {"phone_num":"15251710378","time":1537413823,"sign":"68ac3df6aa2dc8ff72638d2439217d4a"}
        //点击发送验证码，黑卡的接口

        Map params = new HashMap();
        params.put("phone_num", phoneNum);
        params.put("time", System.currentTimeMillis());
        String sign = "";
        params.put("sign", sign);
        HttpUtils.doPost(BaseVariable.baseUrl + "/api/auth/verify", params, new HashMap());
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap();
        params.put("phone_num", "15251710378");
        params.put("time", "1537413823");
        String[] secret = new String[]{};//有可能存在的secret
        String sign = "68ac3df6aa2dc8ff72638d2439217d4a";
        String result = "猜猜看";
        int count = 1;
        while (!validate(result, sign)) {
            StringBuilder url = new StringBuilder(BaseVariable.baseUrl + "/api/auth/verify?");
            params.put("secret", randomStr(count / 26 + 1));
            for (String key : params.keySet()) {
                url.append(key + "=" + params.get(key) + "&");
            }
            result = url.toString().substring(0, url.toString().length() - 1);
            if (count % 26 == 0) {
                System.out.println("已经执行:" + count + "次！");
            }
            count++;
        }


    }

    private static String randomStr(int len) {
        char[] arr = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        char[] randomChar = new char[len];
        int i = 0;
        while (i < len) {
            randomChar[i] = arr[(int) Math.floor(Math.random() * 26)];
            i++;
        }
        StringBuilder s = new StringBuilder();
        for (char charitem : randomChar) {
            s.append(charitem);
        }
        return s.toString();
    }

    private static boolean validate(String str, String sign) {
        boolean flag = (MD5.Bit32(str).equalsIgnoreCase(sign));
        System.out.println("开始尝试:" + str + ",结果:" + flag);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
