package com.api.collect;

import com.bean.Mine;
import com.bean.MineAccount;
import com.cons.BaseVariable;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.util.HttpUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * 收集lord
 */
public class Lord {


    /**
     * 收集lord
     */
    public void collect(String key, String token) {
        String path = "/api/user/mine/collect";
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        Map param = new HashMap();
        param.put("key", key);
        HttpUtils.doPost(BaseVariable.baseUrl + path, param, header);
    }

    /**
     * 查看当前已经产生还没收取的lord
     *
     * @return
     */
    public Mine mines(String token) {
        String url = BaseVariable.baseUrl + "/api/user/mines";
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        return new Gson().fromJson(HttpUtils.doPost(url, new HashMap(), header), new TypeToken<Mine>() {
        }.getType());
    }

    /**
     * 查看当前帐号的所有lord
     *
     * @param token
     * @return
     */
    public MineAccount mineAccount(String token) {
        String path = "/api/user/mine/info";
        return new Gson().fromJson(HttpUtils.doPost(BaseVariable.baseUrl + path, new HashMap(), getTokenHeader(token)), new TypeToken<MineAccount>() {
        }.getType());
    }

    /**
     * 这里适用于上海地区，
     * 上海地区的经纬度范围:120.85 ~ 122.2  E ,30.66 ~ 31.88 N
     * 加上随机数 生成十五位小数的随机经纬度。
     *
     * @return 下标0为经度，1为纬度
     */
    private String[] randomLatLng() {
        double lat = 31.1 + (31.5 - 31.1) * Math.random();
        double lng = 121.10 + (121.90 - 121.10) * Math.random();
//        return new String[]{"121.31806666876044", "31.240294689427763"};
        return new String[]{new BigDecimal(lng).setScale(15, BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(lat).setScale(15, BigDecimal.ROUND_HALF_UP).toString()};
    }

    /**
     * 获取红包地图页面附近可点的红包参数
     *
     * @param token
     * @return
     */
    private List<Map<String, String>> getRedPacketParams(String token) {
        Map params = new HashMap();
        String lng = randomLatLng()[0];
        String lat = randomLatLng()[1];
        System.out.println("随机经纬度:" + lng + " E," + lat + " N");
        params.put("lat", lat);
        params.put("lng", lng);
        String response = HttpUtils.doPost(BaseVariable.baseUrl2 + "/api/envelope/by/lbs", params, getTokenHeader(token));
        Object els = getVal("els", response);
        List<Map> elsList = new Gson().fromJson(new Gson().toJson(els), new TypeToken<List<Map>>() {
        }.getType());
        if (elsList == null || elsList.size() == 0) {
            throw new RuntimeException("初始化红包地图页面失败!!");
        }
        List<Map<String, String>> askList = new ArrayList<>();
        for (Map data : elsList) {
            Map<String, String> tmp = new HashMap();
            tmp.putAll(params);
            tmp.put("secret", "");
            if (data.get("owner") != null && ((LinkedTreeMap) data.get("owner")).size() > 4) {
                tmp.put("owner_name", ((LinkedTreeMap) data.get("owner")).get("real_name").toString());
                tmp.put("owner_phone", ((LinkedTreeMap) data.get("owner")).get("phone_num").toString());
                tmp.put("owner_idcard", ((LinkedTreeMap) data.get("owner")).get("id_card").toString());
                tmp.put("created_at", ((LinkedTreeMap) data.get("owner")).get("created_at").toString());
            }
            if (data.get("token") != null) {//米米红包
                tmp.put("token", data.get("token").toString());
                params.put("eid", "-1");
            } else if (data.get("id") != null) {//玩家的红包
                tmp.put("eid", data.get("id").toString().replace(".0", ""));
            } else {
                //广告
            }
            askList.add(tmp);
        }
        return askList;
    }

    /**
     * 收集红包
     * <p>
     * //     * @param _token 不是登录的token
     * <p>
     * //     * @param lat 纬度 (手机自身API获取的)
     * //     * @param lng 经度 (手机自身API获取的)
     * {"lat":31.209531689977965,"lng":121.31383381521583}
     *
     * @return
     */
    public List<String> collectRedPacket(String token) {
        List<String> redMoneys = new ArrayList<>();
        List<Map<String, String>> params = getRedPacketParams(token);
        for (Map<String, String> param : params) {
            HashMap requestParam = new HashMap();
//            requestParam.put("lat", param.get("lat"));
//            requestParam.put("lng", param.get("lng"));
            requestParam.put("lat", "31.209532639219223");
            requestParam.put("lng", "121.31380987251161");//固定经纬度
            requestParam.put("secret", param.get("secret"));
            requestParam.put("eid", param.get("eid"));
            String response = HttpUtils.doPost(BaseVariable.baseUrl + "/api/envelope/ask", requestParam, getTokenHeader(token));
            try {
                Map resMap = new Gson().fromJson(response, new TypeToken<Map>() {
                }.getType());
                if (!resMap.get("code").toString().replace(".0", "").equals("0")) {
                    System.out.println(resMap.get("data") == null ? null : resMap.get("data").toString());
                    continue;
                }
            } catch (Exception e) {
                System.err.println("红包已经被抢过了!,经度:" + param.get("lng") + ",纬度:" + param.get("lat"));
                continue;
            }
            redMoneys.add("感谢老铁:" + param.get("owner_name") + "于" + param.get("created_at") + "发的" + getVal("money", response).toString() + "元红包");
            //休息1秒继续
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        recordRealNameIDcard(params);
        return redMoneys;
    }

    //收集姓名身份证
    private void recordRealNameIDcard(List<Map<String, String>> params) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        String[] columns = new String[]{"owner_name", "owner_phone", "owner_idcard", "created_at"};
        for (int i = 0; i < params.size(); i++) {
            Map<String, String> param = params.get(i);
            Row row = sheet.createRow(i);
            for (int j = 0; j < columns.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(param.get(columns[j]));
            }
        }
        try {
            if (params.size() > 0 && params.get(0).size() > 4) {//需要通过验证码
                File file = new File("/home/file/kajsar_id_card_" + System.currentTimeMillis() + ".xlsx");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分红信息
     */
    public Map<String, String> partitionInfo(String token) {
        String url = BaseVariable.baseUrl + "/api/participation/info";
        String result = HttpUtils.doPost(url, new HashMap(), getTokenHeader(token));
        Map data = getData(result);
        String total = data.get("total").toString();
        String all_lord = data.get("all_lord").toString();
        String credit = data.get("credit").toString();
        Map partionInfo = new HashMap();
        partionInfo.put("total", total);
        partionInfo.put("credit", credit);
        return partionInfo;
    }

    /**
     * 领取分红
     *
     * @param token
     * @return 领取失败?
     */
    public String collectPartition(String token) {
        String url = BaseVariable.baseUrl + "/api/participation/collect";
        String result = HttpUtils.doPost(url, new HashMap(), getTokenHeader(token));
        Map map = new Gson().fromJson(result, new TypeToken<Map>() {
        }.getType());
        return map.get("data").toString();
    }

    /**
     * 分红历史
     *
     * @param token
     * @return
     */
    public List<String> partitionHistory(String token) {
        String url = BaseVariable.baseUrl + "/api/participation/collect/history";
        String result = HttpUtils.doPost(url, new HashMap(), getTokenHeader(token));
        Object history = getVal("history", result);
        List<Map> historyList = new Gson().fromJson(new Gson().toJson(history), new TypeToken<List<Map>>() {
        }.getType());
        return historyList.stream().map(it -> it.get("after_credit") + ",更新时间:" + it.get("updated_at")).collect(toList());
    }

    /**
     * 从data中获取某个key的值
     *
     * @param key
     * @return
     */
    public Object getVal(String key, String source) {
        Gson gson = new Gson();
        Map map = gson.fromJson(source, new TypeToken<Map>() {
        }.getType());
        if (map.get("data") == null || map.get("data") instanceof String) {
            return map.get("data");
        }
        Map dataMap = gson.fromJson(gson.toJson(map.get("data")), new TypeToken<Map>() {
        }.getType());
        return dataMap.get(key);
    }

    /**
     * 获取data
     *
     * @return
     */
    public Map getData(String source) {
        Gson gson = new Gson();
        Map map = gson.fromJson(source, new TypeToken<Map>() {
        }.getType());
        if (map.get("data") == null || map.get("data") instanceof String) {
            return new HashMap();
        }
        Map dataMap = gson.fromJson(gson.toJson(map.get("data")), new TypeToken<Map>() {
        }.getType());
        return dataMap;
    }

    public Map getTokenHeader(String token) {
        Map header = new HashMap();
        header.put("authorization", "Bearer " + token);
        header.put("accept-language", "zh-cn");
        return header;
    }

}
