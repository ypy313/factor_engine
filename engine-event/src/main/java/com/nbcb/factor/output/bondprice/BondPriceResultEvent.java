package com.nbcb.factor.output.bondprice;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.output.MarketProcessingOutPut;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

import static com.nbcb.factor.common.Constants.BOND_PRICE_RESULT_EVENT;
import static com.nbcb.factor.common.Constants.MARKET_PROCESSING_BOND_PRICE_RESULT;

/**
 * 输出现价排序计算参数-用于factor-web展示
 */
@Getter@Setter@ToString
public class BondPriceResultEvent implements SymbolOutputEvent<BondPriceOutputResult>, OutputEvent, Serializable {
    private static final long serialVersionUID = 1L;
    private String eventId;//事件id
    private String eventName;//事件名称
    private String srcTimestamp;//来源时间，路透行情里的原始时间
    private String resTimestamp;//flink接收行情时间
    private String createTime;//结果产生时间
    private String updateTime;//更新最后时间
    private String resultType;//结果类型
    private BondPriceOutputResult eventData;

    public BondPriceResultEvent(MarketProcessingOutPut event) {
        this.eventId = BOND_PRICE_RESULT_EVENT+"_"+event.getEventId();
        this.eventName = BOND_PRICE_RESULT_EVENT;
        this.srcTimestamp = event.getSrcTimestamp();//原始时间
        this.resTimestamp = event.getResTimestamp();//接收行情的时间
        this.updateTime = DateUtil.getSendingTime();//处理完成的时间
        this.createTime = DateUtil.getSendingTime();//处理完成的时间
        this.resultType = MARKET_PROCESSING_BOND_PRICE_RESULT;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public String getPkKey() {
        return "";
    }

    @Override
    public String getCompressFlag() {
        return "";
    }

    @Override
    public String getInstrumentId() {
        return "";
    }

    @Override
    public String eventType() {
        return "";
    }
}
