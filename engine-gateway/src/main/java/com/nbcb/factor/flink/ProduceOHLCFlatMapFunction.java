package com.nbcb.factor.flink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.*;
import com.nbcb.factor.enums.PeriodEnum;
import com.nbcb.factor.enums.ResultTypeEnum;
import com.nbcb.factor.flink.mtime.PmSGEDateTimeUtils;
import com.nbcb.factor.flink.mtime.PmSHFEDateTimeUtils;
import com.nbcb.factor.flink.mtime.ReuterDateTimeUtils;
import com.nbcb.factor.output.FxOhlcResultOutputEvent;
import com.nbcb.factor.output.OhlcDetailResult;
import com.nbcb.factor.utils.OhlcRedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.nbcb.factor.common.LoadConfig.getProp;

/**
 * 生产不同周期的bar
 */
@Slf4j
public class ProduceOHLCFlatMapFunction implements FlatMapFunction<FxOhlcResultOutputEvent,
        Tuple2<String, FxOhlcResultOutputEvent>> {
    private static String type;

    public ProduceOHLCFlatMapFunction(String type) {
        this.type = type;
    }

    /**
     * bar线 不存在
     */
    private static final byte BAR_NOT_EXISIS = -1;
    /**
     * bar线 有效
     */
    private static final byte BAR_OK = 1;
    /**
     * bar线 已过期
     */
    private static final byte BAR_EXPIRED = 0;

    /**
     * 货币对各周期bar线在内存的快照 {symbol:{period:obj}}
     */
    private final Map<String, Map<String, FxOhlcResultOutputEvent>> symbolMap = new ConcurrentHashMap<>();
    /**
     * 货币对周期对应事件记录Map key-symbol-period value-beginTime
     */
    private final Map<String, String> beginTimeMap = new ConcurrentHashMap<>();


    private final PeriodEnum[] USED_PERIODS = Arrays.stream(getProp().getProperty("ohlc.period.list")
            .split(",")).map(PeriodEnum::parse)
            .filter(e -> e != PeriodEnum.NONE).toArray(PeriodEnum[]::new);

    static {
        //预初始化
        RedisUtil instance = RedisUtil.getInstance();
    }


    @Override
    public void flatMap(FxOhlcResultOutputEvent ohlc, Collector<Tuple2<String, FxOhlcResultOutputEvent>> out) throws Exception {
        OhlcDetailResult eventData = ohlc.getEventData();
        String symbol = eventData.getSymbol();//货币对
        String eventTime = eventData.getBeginTime();//200ms线区起始时间
        //同个bar的更新+发实时
        //过期的发历史
        FxOhlcResultOutputEvent temp;
        for (PeriodEnum period : USED_PERIODS) {
            switch (getBarInfo(symbol, period, eventTime)) {
                case BAR_NOT_EXISIS:
                    temp = createNewBar(ohlc, period);
                    sendDetail(temp, period, out);
                    break;
                case BAR_OK:
                    temp = updateBar(ohlc, period);
                    sendDetail(temp, period, out);
                    break;
                case BAR_EXPIRED:
                    sendHistory(symbol,period,out);
                    temp = createNewBar(ohlc,period);
                    sendDetail(temp,period,out);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 创建新bar：全新|在过期的历史上更新
     * @param ohlc 200ms结果
     * @param period 周期
     * @return 新bar
     */
    private FxOhlcResultOutputEvent createNewBar(FxOhlcResultOutputEvent ohlc, PeriodEnum period) {
        FxOhlcResultOutputEvent newOhlc = ohlc.clone();
        String symbol = ohlc.getEventData().getSymbol();
        //更新周期
        newOhlc.updatePeriod(period,symbol.contains("@")? StrategyNameEnum.PM.getName() : StrategyNameEnum.TA.getName());

        OhlcDetailResult data = newOhlc.getEventData();
        //更新周期区间
        Tuple2<String,String> timeWindow = null;

        if (symbol.endsWith(MarketTypeEnum.SGE.getEndName())) {
            //金交所
            timeWindow = PmSGEDateTimeUtils.getTimeWindow(data.getBeginTime(),period);
        }else if (symbol.endsWith(MarketTypeEnum.SHFE.getEndName())){
            //上清所
            timeWindow = PmSHFEDateTimeUtils.getTimeWindow(data.getBeginTime(),period);
        }else {
            //行情平台的外汇与贵金属信息
            timeWindow = ReuterDateTimeUtils.getTimeWindow(data.getBeginTime(),period);
        }
        //没有时间区间，则不需要融合，返回空
        if (timeWindow==null) {
            return null;
        }
        data.setBeginTime(timeWindow.f0);
        data.setEndTime(timeWindow.f1);

        //获取货币对vs周期
        symbolMap.compute(data.getSymbol(),(k, periodMap)->{
            if (periodMap== null) {
                periodMap=new ConcurrentHashMap<>(16);
            }
            periodMap.put(period.getCode(),newOhlc);
            return periodMap;
        });
        return newOhlc;

    }

    /**
     * 用200ms的bar线 更新period周期bar线
     * @return 更新后的bar线
     */
    private FxOhlcResultOutputEvent updateBar(final FxOhlcResultOutputEvent ohlc, PeriodEnum periodEnum){
        String key = ohlc.getEventData().getSymbol();
        FxOhlcResultOutputEvent oldOhlc = Optional.ofNullable(symbolMap.get(key))
                .map(e -> e.get(periodEnum.getCode())).orElse(null);
        if (oldOhlc != null) {
            calcOHLC(oldOhlc,ohlc);
            oldOhlc.updatePeriod(periodEnum,key.contains("@")?StrategyNameEnum.PM.getName():StrategyNameEnum.TA.getName());
        }
        return oldOhlc;
    }

    /**
     * 更新ohlc
     */
    public static void calcOHLC(FxOhlcResultOutputEvent oldOhlc, FxOhlcResultOutputEvent newOhlc){
        //主表
        oldOhlc.setEventId(newOhlc.getEventId());
        oldOhlc.setPkKey(newOhlc.getPkKey());
        newOhlc.setEventName(newOhlc.getEventName());
        newOhlc.setSrcTimestamp(newOhlc.getSrcTimestamp());
        newOhlc.setResTimestamp(newOhlc.getResTimestamp());
        newOhlc.setUpdateTime(newOhlc.getUpdateTime());

        //子表
        OhlcDetailResult prev = oldOhlc.getEventData();
        OhlcDetailResult after = newOhlc.getEventData();
        //o不变
        //h
        if (prev.getHighPrice()<after.getHighPrice()) {
            prev.setHighPrice(after.getHighPrice());
            prev.setHighEventId(after.getHighEventId());
        }
        //l
        if (prev.getLowPrice()>after.getLowPrice()) {
            prev.setLowPrice(after.getLowPrice());
            prev.setLowEventId(after.getLowEventId());
        }
        //c
        prev.setClosePrice(after.getClosePrice());
        prev.setCloseEventId(after.getCloseEventId());
    }


    /**
     * 发送明细到Kafka
     * 更新reedis
     */
    private void sendDetail(FxOhlcResultOutputEvent ohlc, PeriodEnum period
            , Collector<Tuple2<String,FxOhlcResultOutputEvent>> out){
        if (ohlc !=null) {
            OhlcDetailResult data = ohlc.getEventData();
            OhlcRedisUtils.setOhlcOutPut(data.getSymbol(), period.getCode(),ohlc);
            out.collect(Tuple2.of(getProp().getProperty(FxLoadConfig.PRODUCER_DETAIL_TOPIC),ohlc));
        }
    }

    /**
     * 发送历史到kafka，删除reids
     */
    private void sendHistory(String symbol,PeriodEnum period,Collector<Tuple2<String,FxOhlcResultOutputEvent>> out){
        Optional.ofNullable(symbolMap.get(symbol)).map(e -> e.get(period.getCode())).ifPresent(ohlc->{
            OhlcDetailResult data = ohlc.getEventData();
            //判断当前货币对与周期之间是否有发送过，发送过则不发送历史
            String key = symbol +"_"+period.getCode();
            String beginInfo = beginTimeMap.compute(key,(k,v)->{
                if (v==null) {
                    v=OhlcRedisUtils.getOhlcHistoryBeginTime(key);
                }
                return v;
            });
            String beginTime = data.getBeginTime();
            if (StringUtils.isEmpty(beginInfo)||(StringUtils.isNotEmpty(beginInfo) && beginTime.compareTo(beginInfo)>0)) {
                data.setSummaryType(ResultTypeEnum.HIS.getCode());
                OhlcRedisUtils.delOhlcOutput(type+data.getSymbol(),data.getPeriod());
                OhlcRedisUtils.setOhlcHistoryBeginTime(key,beginTime);
                beginTimeMap.put(key,beginTime);
                out.collect(Tuple2.of(getProp().getProperty(FxLoadConfig.PRODUCER_HIS_TOPIC),ohlc));
            }
        });
    }

    /**
     * 判断当前k线的融合属于哪个区间
     * @param symbol 货币对
     * @param period 周期
     * @param eventTime 事件时间
     * @return
     */
    private byte getBarInfo(String symbol, PeriodEnum period, String eventTime) {
        Map<String, FxOhlcResultOutputEvent> periodMap = symbolMap
                .compute(symbol, (k, v) -> v != null ? v : new ConcurrentHashMap<>(16));
        FxOhlcResultOutputEvent snapshot = periodMap.computeIfAbsent(period.getCode(), key -> {
            try {
                return OhlcRedisUtils.getOhlcOutPut(symbol, key);
            } catch (JsonProcessingException e) {
                log.error("反序列化失败", e);
                return null;
            }
        });

        if (snapshot == null) {
            return BAR_NOT_EXISIS;
        }
        if (timeInRange(eventTime,snapshot.getEventData().getBeginTime(),snapshot.getEventData().getEndTime())) {
            return BAR_OK;
        }
        return BAR_EXPIRED;
    }

    private boolean timeInRange(String eventTime,String beginTime,String endTime) {
        return eventTime.compareTo(beginTime)>=0 && eventTime.compareTo(endTime)<0;
    }
}
