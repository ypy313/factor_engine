package com.nbcb.factor.event;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * 策略命令 包括启动 暂停 重新加载等
 */
@Data
@ToString
public class StrategyCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TOPIC = "FACTOR_ANALYZE_STRATEGY_CMD";
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    private final long createdTimeStamp = System.currentTimeMillis();

    private String cmd;//命令
    private String operator;//操作人
    private String strategyInstanceId;//策略实例
    private String cmdTime;//命令时间

    private String indexCategory;//指标类型
    private String cmdType;//ALL-加载全部（默认） ，SPECIFY-指定
    private List<SpecifyData> data = new ArrayList<SpecifyData>();//指定时的集合

    private String strategyName;//策略名称 如 TA
    public String getTopic(){return TOPIC;}
    public String getKey(){return KEY;}
}
