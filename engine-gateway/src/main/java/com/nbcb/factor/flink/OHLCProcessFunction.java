package com.nbcb.factor.flink;

import com.nbcb.factor.common.StrategyNameEnum;
import com.nbcb.factor.event.SymbolOutputEvent;
import com.nbcb.factor.event.forex.FactorFxPmAllMarketData;
import com.nbcb.factor.flink.mtime.ReuterDateTimeUtils;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import com.nbcb.factor.output.FxPmMarketDataOutput;
import com.nbcb.factor.output.OhlcDetailResult;
import com.nbcb.factor.utils.TaScriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * 计数窗口计算ohlc
 * Tuple8<String,Double,Double,Double,Double,Long,Long,String>
 *  货币对-开盘价-最高价-最低价-收盘价-bid/ask发生事件（事件时间）-创建时间-事件ID(connector生成)
 *  bid/ask发生事件（事件时间）两两比较最后行情的事件时间
 */
@Slf4j
public class OHLCProcessFunction extends ProcessWindowFunction<SymbolOutputEvent, FxOhlcResultOutputEvent,String, TimeWindow> {

    @Override
    public void process(String symbol, ProcessWindowFunction<SymbolOutputEvent, FxOhlcResultOutputEvent, String, TimeWindow>.Context context
            , Iterable<SymbolOutputEvent> elements, Collector<FxOhlcResultOutputEvent> out) throws Exception {
        //只有行情数据
        FactorFxPmAllMarketData marketData = ((FxPmMarketDataOutput) elements.iterator().next()).getEventData();
        try{
            //中间价
            double midPrice = TaScriptUtils.clacMidPrice(marketData.getBid(), marketData.getAsk());
            //初始化ohlc
            double open = midPrice;
            double high = open;
            double low = open;
            double close = open;
            String openEventId = marketData.getEventId();
            String highEventId = openEventId;
            String lowEventId = openEventId;
            String closeEventId = openEventId;
            String ric = null;
            String source = null;
            FxPmMarketDataOutput lastOutputEvent = null;
            int count = 0;

            for (SymbolOutputEvent price : elements) {
                marketData = (FactorFxPmAllMarketData) price.getEventData();
                //计算中间价
                double midPriceNow = TaScriptUtils.clacMidPrice(marketData.getBid(), marketData.getAsk());
                //开盘价与收盘价为当前价
                String eventId = marketData.getEventId();
                close = midPriceNow;
                closeEventId = eventId;
                //最高加高地域当前价，设置为当前为最高价
                if (high<midPriceNow) {
                    high = midPriceNow;
                    highEventId = eventId;
                }
                //最低价低于当前价，设置为当前为最低价
                if (low>midPriceNow) {
                    low = midPriceNow;
                    lowEventId = eventId;
                }
                lastOutputEvent = (FxPmMarketDataOutput) price;
                ric = marketData.getRic();
                source = marketData.getSource();
                count++;
            }
            TimeWindow window = context.window();
            long start = window.getStart();
            long end = window.getEnd();
            //结果构建
            String strategyName;
            if (symbol.contains("@")) {
                //贵金属
                strategyName = StrategyNameEnum.PM.getName();
            }else {
                //外汇
                strategyName = StrategyNameEnum.TA.getName();
            }
            OhlcDetailResult detailResult = buildData(symbol,ric,source,open,high,low,close,openEventId
                    ,highEventId,lowEventId,closeEventId,start,end,strategyName);
            FxOhlcResultOutputEvent record = buildRecord(lastOutputEvent,detailResult);
            //输出
            out.collect(record);
            if (log.isDebugEnabled()) {
                log.debug("[ {} ~ {} ) 共{}个行情，ohlc:{}",detailResult.getBeginTime()
                        ,detailResult.getEndTime(),count,record.getPkKey());
            }
        } catch (Exception e){
            log.error("事件ID:{},债券对：{}，计算发送错误：{} ！",marketData.getEventId(),symbol,e.getMessage());
        }
    }

    /**
     * ohlc详情
     */
    private OhlcDetailResult buildData(String symbol, String ric, String source,
                                       double open,double high, double low, double close,
                                       String openEventId,String highEventId, String lowEventId, String closeEventId,
                                       long start, long end, String strategyName) {
        OhlcDetailResult detail = OhlcDetailResult.createDetail(symbol, null, strategyName);
        detail.setRic(ric);
        detail.setSource(source);
        detail.setOhlc(open,high,low,close);
        detail.setOhlcId(openEventId,highEventId,lowEventId,closeEventId);
        detail.setBeginTime(ReuterDateTimeUtils.format(start));
        detail.setEndTime(ReuterDateTimeUtils.format(end));
        return detail;
    }

    /**
     * @param outputEvent 外汇行情event
     * @param detailResult detail data
     * @return
     */
    private FxOhlcResultOutputEvent buildRecord(FxPmMarketDataOutput outputEvent, OhlcDetailResult detailResult) {
        FxOhlcResultOutputEvent record = new FxOhlcResultOutputEvent(outputEvent, detailResult);
        record.setSrcTimestamp(outputEvent.getSrcTimestamp());
        record.setResTimestamp((outputEvent.getResTimestamp()));
        return record;
    }
}
