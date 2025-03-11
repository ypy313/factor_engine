package com.nbcb.factor.output.forex;

import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.output.OhlcDetailResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 计算直白哦输出结果
 * @param <T>
 */
@Getter@Setter@ToString
public class ForexOhlcOutputEvent<T extends OhlcDetailResult & Serializable>
        implements SymbolOutputEvent<T>,Serializable {
    private static final long serialVersionUID = 1L;
    private String eventId;//事件id
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间，路透行情里的原始行情
    private String resTimestamp;//flink接收行情时间
    private String createTime;//结果产生时间
    private String updateTime;//更新最后时间
    private String resultType;//结果类型
    private T ohlcDetailResult;
    private Map<String,Object> calculationResultMap = new HashMap<>();

    @Override
    public String eventType() {
        return null;
    }

    @Override
    public T getEventData() {
        return this.ohlcDetailResult;
    }

    @Override
    public String getInstrumentId() {
        return ohlcDetailResult.getInstanceId();
    }
}
