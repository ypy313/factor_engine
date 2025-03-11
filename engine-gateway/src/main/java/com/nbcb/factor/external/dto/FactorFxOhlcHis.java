package com.nbcb.factor.external.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter@Setter@ToString
public class FactorFxOhlcHis {
    //对象id
    private String id;
    //事件id
    private String eventId;
    //RIC码
    private String ric;
    //资产代码
    private String symbol;
    //行情来源
    private String source;
    //k线窗口
    private String period;
    //Bar 开始时间
    private String beginTime;
    //Bar 结束时间
    private String endTime;
    //交易日期
    private String tradeDate;
    //开始价格
    private Double openPrice;
    //最高价格
    private Double highPrice;
    //最低价格
    private Double lowPrice;
    //最后价格
    private Double closePrice;
    //交易量
    private String volume;
    //新增时间
    private String createTime;
    //修改时间
    private String updateTime;

}

