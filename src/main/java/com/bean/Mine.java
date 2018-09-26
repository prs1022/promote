package com.bean;

import lombok.Data;

import java.util.List;

@Data
public class Mine {
    private int status;
    private int code;
    private MineData data;

    @Data
    public class MineData {
        private List<MineItem> mines;
    }

    @Data
    public class MineItem {
        private String key;
        private double credit;
        private long time;
    }
}
