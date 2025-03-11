package com.nbcb.factor.handler;

import com.nbcb.factor.entity.localcurrency.LocalCurrencyStrategyInstanceResult;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.flink.aviatorfun.entity.OhlcParam;
import com.nbcb.factor.output.OhlcDetailResult;

import java.util.List;

public interface CalculationHandler<T extends LocalCurrencyStrategyInstanceResult
        , D extends OhlcDetailResult, F extends List<OhlcParam>> {

    /**
     * 消息处理器
     * @param t 配置
     * @param d 消息实体(此处暂时为K线数据)
     * @param f ohlc行特队列
     * @return 处理后的SymbolOutputEvent列表
     */
    List<SymbolOutputEvent<D>> handler(T t, D d, F f);

    /**
     * 获取计算类型
     *
     * @return 消息类型
     */
    String getCalculationType();
}
