package com.bean;

import lombok.Data;

@Data
public class MineAccount {
    private int status;
    private int code;
    private MineAccountData data;

    @Data
    public class MineAccountData {
        private int user_id;//194168
        private double credit;
        private long capacity;//算力
        private String created_at;
        private String updated_at;
        private String deleted_at;
        private double cost;//不知道啥意思 1.45912452
    }
}
