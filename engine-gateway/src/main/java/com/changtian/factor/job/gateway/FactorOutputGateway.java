package com.changtian.factor.job.gateway;

import com.changtian.factor.event.OutputEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 因子输出网关
 * 将因子策略计算结果，写入日志，kafka或者数据库
 */
public interface FactorOutputGateway extends Serializable {
    public void init(Map paraMap);//初始化配置
    public void output(List<OutputEvent> result);//因子结果输出
}
