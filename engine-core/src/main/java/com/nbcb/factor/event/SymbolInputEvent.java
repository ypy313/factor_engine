package com.nbcb.factor.event;

import java.io.Serializable;
import java.util.List;

/**
 * 货币对事件父类
 * @param <T>
 */
public interface SymbolInputEvent <T> extends InputEvent, Serializable {
    void setEventData(T eventData);//设置数据
    String getStrategyName();//获取策略名称
    List<String> getRelationId();//得到关联id
    boolean addRelationId(String relationId);//新增关联id
}
