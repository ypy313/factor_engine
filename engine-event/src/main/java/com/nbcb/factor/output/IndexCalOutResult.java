package com.nbcb.factor.output;

import com.nbcb.factor.monitorfactor.TradeSignal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 指标计算结果配置
 */
@Setter@Getter@ToString
public class IndexCalOutResult extends OhlcDetailResult{
    private String monitorId;//监控id
    private String monitorName;//监控名称
    private String triggerRule;//监控表达式
    private Boolean monitorResult;//监控表达式计算结果
    private String action;//交易建议
    private TradeSignal signal;//信号
    private Integer monitorModel;//模式
    private String signalPopup;//弹窗设置
    private String reminderCycle;//提醒周期
    private String sendingText;//短信提醒 发送/不发送
    private Long reminderInterval;//提醒周期 时间毫秒
    private String signalDuration;//信号持续时间 毫秒
    private String assetPoolId;//资产池id
    private String assetPoolName;//资产池名称
    private String pushStartTime;//因子信号推送开始
    private String pushEndTime;//因子信号推送结束时间

    public IndexCalOutResult convert(OhlcDetailResult ohlcDetail){
        this.setSymbol(ohlcDetail.getSymbol());
        this.setPeriod(ohlcDetail.getPeriod());
        this.setSource(ohlcDetail.getSource());
        this.setSummaryType(ohlcDetail.getSummaryType());
        this.setBeginTime(ohlcDetail.getBeginTime());
        this.setEndTime(ohlcDetail.getEndTime());
        this.setOpenPrice(ohlcDetail.getOpenPrice());
        this.setHighPrice(ohlcDetail.getHighPrice());
        this.setLowPrice(ohlcDetail.getLowPrice());
        this.setClosePrice(ohlcDetail.getClosePrice());
        this.setRelationId(ohlcDetail.getRelationId());
        this.setRic(ohlcDetail.getRic());
        return this;
    }
}
