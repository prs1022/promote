package com.api;

import java.util.ArrayList;
import java.util.List;

public class IDTest {
    public static void main(String[] args) {
        int[] arr = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        int[] cal_rs = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};//校验核对算出来的值
        int[] id_last = new int[]{1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2};//校验核对对应的身份证最后一位数字
        List<Integer> cal_list = new ArrayList();
        for (int i : cal_rs) {
            cal_list.add(i);
        }
        String[][] idinfo = new String[][]{
                {"贺若灵", "142201200005307527", "女", "山西省忻州地区忻州市"},
                {"宋凌蝶", "532624197109183287", "女", "云南省文山壮族苗族自治州麻栗坡县"},
                {"姚含海", "511521199607212167", "女", "四川省宜宾市宜宾县"},
                {"曾新梅", "130902198006062948", "女", "河北省沧州市新华区"},
                {"姚醉波", "152500198007059768", "女", "内蒙古自治区锡林郭勒盟"},
                {"范黎明", "152800197009277031", "男", "内蒙古自治区巴彦淖尔盟"},
                {"苏宏深", "130824199612304932", "男", "河北省承德市滦平县"},
                {"石天泽", "210212197201245571", "男", "辽宁省大连市旅顺口区"},
                {"邱阳冰", "330721197611103550", "男", "浙江省金华市金华县"},
                {"史兴为", "445302199904284613", "男", "广东省云浮市云城区"},
                {"孟波鸿", "52020019850623463X", "男", "贵州省六盘水市"},
                {"黎芷珊", "430211198711122706", "女", "湖南省株洲市天元区"},
                {"范痴海", "410223199812280128", "女", "河南省开封市尉氏县"},
                {"邱依瑶", "321201197509037020", "女", "江苏省泰州市市辖区"}
        };
        String[] ids = new String[idinfo.length];
        for (int i = 0; i < idinfo.length; i++) {
            ids[i] = idinfo[i][1];
        }
        //        char i = '1';
//        System.out.println(new Integer(i-'0'));
        for (String tmp : ids) {
            char[] ca = tmp.substring(0, tmp.length() - 1).toCharArray();
            char last_num = tmp.charAt(tmp.length() - 1);
            int sum = 0;
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i] * new Integer(ca[i] - '0');//除了最后一位身份证前17位排除有X的情况
            }
            int result = id_last[cal_list.indexOf(sum % 11)];
            if (last_num == 'X' && result == 10) {
                System.out.println("成功");
            } else if ((last_num - '0') == result) {
                System.out.println("成功");
            } else {
                System.out.println("失败的ID：" + tmp);
            }
        }
    }
}
