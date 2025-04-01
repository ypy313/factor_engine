package com.changtian.factor.monitorfactor;

import java.util.Map;
import java.util.Objects;

/**
 * 单子因子计算
 * 例如：根据输入价差、计算中债百分位数
 */
public interface Factor <T>{
    void init(Map paraMap);//初始化配置
    T calc(Map context);//因子计算 例如：根据价差，计算当前价差所在中债收益估值价差百分位数
    Map<String, Objects> calcSignal(Map factorOutput);//根据calc的输出，计算TradeSignal和触发规则
    String getFactorName();//获取因子名称
}
