package com.bean;

import lombok.Data;

@Data
public class UserInfo {
    private int status;//1
    private int code;
    private UserData data;

    @Data
    public class UserData {
        private int id;
        private String phone_num;
        private String real_name;//编码
        private String id_card;
        private String invite_code;//885074
        private int parent_id;//2
        private String pic_name;
        private String remeber_token;
        private int status;//1
        private String ip;//117.136.8.79
        private String device_id;//""
        private String jiguang_id;//推送ID，18171adc03243544951
        private String union_id;
        private String created_at;
        private String updated_at;
        private String deleted_at;
        private int bind;//1
    }
}
