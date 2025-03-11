package com.nbcb.factor.flink;

import cn.hutool.core.date.DateUnit;
import com.nbcb.factor.common.Constants;
import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.common.StringUtil;
import com.nbcb.factor.enums.DataTypeEnum;
import com.nbcb.factor.enums.PeriodEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.job.engine.FxSyncEventEngine;
import com.nbcb.factor.monitor.signal.SignalCacheMap;
import com.nbcb.factor.output.FxTaTradeSignalOutputEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.nbcb.factor.common.LoadConfig.getProp;

/**
 * 信号过滤方法处理
 */
@Slf4j
public class OHlCSignalFilterFunction extends ProcessFunction<OutputEvent, OutputEvent> {
    public static final String TRADESIGNAL = "TradeSignal";
    private RedisUtil redisUtil;
    private static final String FACTOR_PUSH_SIGNAL_INFO = StringUtil
            .redisKeyFxAndPmJoint(DataTypeEnum.PUSH_SIGNAL_INFO.getKey());
    //存储当前分钟对应的发送条数
    private Map<String, Integer> sendNumMap = new ConcurrentHashMap<>();
    private static final OutputTag<OutputEvent> textMessageOutputTag = new OutputTag<OutputEvent>
            ("text-message-output") {
        private static final long serialVersionUID = 1L;
    };
    private int sendNum = 0;
    //每分钟限制800笔
    private final int workPlatSendNum = Integer.parseInt(getProp().getProperty("work.platform.send.num"));
    private SignalCacheMap signalCacheMap;

    @Override
    public void processElement(OutputEvent value
            , ProcessFunction<OutputEvent, OutputEvent>.Context ctx
            , Collector<OutputEvent> out) throws Exception {
        String resultType = value.getResultType();
        if (!TRADESIGNAL.equals(resultType)) {
            return;
        }
        if (!FxSyncEventEngine.isIsUpdateTimeMap()) {
            HashSet<String> signalTimeMap = new HashSet<>(signalCacheMap.getSignalTimeRedisList());
            for (String key : signalTimeMap) {
                redisUtil.delKeyString(FACTOR_PUSH_SIGNAL_INFO, key);
            }
            signalCacheMap.clearSignalTimeRedisList();
            FxSyncEventEngine.setIsUpdateTimeMap(true);
        }
        FxTaTradeSignalOutputEvent signal = (FxTaTradeSignalOutputEvent) value;
        FxTaTradeSignalOutputEvent.SignalDetailResult eventData = signal.getEventData();

        //过滤出信号产生时间不属于实例推送时间的数据
        String srcTimestamp = signal.getSrcTimestamp();
        boolean pushFlag = StringUtils.isNotEmpty(eventData.getPushStartTime())
                && StringUtils.isNotEmpty(eventData.getPushEndTime())
                && !filterCompareSrcTime(srcTimestamp, eventData);
        if (pushFlag) {
            log.info("instanceId:{} srcTime:{} not within the push time range {} ---{}",
                    eventData.getInstanceId(), srcTimestamp, eventData.getPushStartTime(), eventData.getPushEndTime());
            return;
        }
        String mapKey = eventData.getInstanceId() + "_" + eventData.getMonitorId() + "_" + eventData.getTriggerRule();
        //提醒周期为空或者为周期提醒时
        if (StringUtils.isEmpty(eventData.getReminderCycle()) ||
                Constants.MONITOR_NORMAl_REMINDER.equals(eventData.getReminderCycle())){
            //普通提醒
            //有-判断现由的值是否大于之前存储的时间（一分钟）大于-更新MAP
            String time = signalCacheMap.getSignalTimeMap().get(mapKey);
            //redis中时间存储不存在-时间为0，存在-计算两时间插值，普通提醒中，进行限流，一分钟推送一笔
            long minus = (time != null) ?
                    DateUtil.timeDiffer(time, srcTimestamp, DateUtil.DT_FORMAT_PATTERN, DateUnit.MINUTE) :1;
            //如果没有||过期
            //当redis中没有存在当前key或存在但是时间间隔已经超过一分钟，推送数据至kafka并保存redis
            if (minus>0) {
                //更新时间
                signalCacheMap.setSignalTimeMap(mapKey,srcTimestamp);
                redisUtil.setString(FACTOR_PUSH_SIGNAL_INFO, mapKey,srcTimestamp);
                out.collect(value);
                log.info("the signal output to the FACTOR_WEB_VIEW_SIGNAL eventId:{}",value.getEventId());
            }
        }else{
            //周期提醒
            //有-判断现由的值是否大于之前存储的时间（一分钟） 大于-更新Map
            String time = signalCacheMap.getSignalTimeMap().get(mapKey);
            //redis中时间存储不存在-时间为0，存在-计算两时间差值
            Long reminderInterval = eventData.getReminderInterval();
            if (reminderInterval == null) {
                //如果为空则为旧配置数据，时间间隔为该配置的ohlc数据，计算出当前时间到下一下推送信号的间隔时间
                String period = eventData.getPeriod();
                PeriodEnum periodEnum = PeriodEnum.parse(period);
                LocalDateTime nowTime = LocalDateTime.now();
                LocalDateTime endTime = nowTime.plus(periodEnum.getValue(), periodEnum.getUnit());
                reminderInterval = ChronoUnit.MILLIS.between(nowTime, endTime);
            }
            long millisecondDiff = (time !=null)?
                    DateUtil.timeDiffer(time,srcTimestamp,DateUtil.DT_FORMAT_PATTERN, DateUnit.MS)
                    :reminderInterval+1;

            if (millisecondDiff>reminderInterval) {
                //更新时间
                signalCacheMap.setSignalTimeMap(mapKey,srcTimestamp);
                redisUtil.setString(FACTOR_PUSH_SIGNAL_INFO,mapKey,srcTimestamp);
                out.collect(value);
                log.info("the signal output to the FACTOR_WEB_VIEW_SIGNAL eventId:{}",value.getEventId());
                textMessageFilter(eventData,ctx,value);
            }
        }
    }

    private void textMessageFilter(FxTaTradeSignalOutputEvent.SignalDetailResult eventData,
                                   ProcessFunction<OutputEvent,OutputEvent>.Context ctx,OutputEvent value){
        if (Constants.MONITOR_SEND.equals(eventData.getSendingText())) {
            //一分钟限制笔数为800笔 时间格式key 格式 yyyy-MM-dd HH:mm:ss
            String nowDate = DateUtil.getForexTime();
            if (sendNumMap.containsKey(nowDate)) {
                //该分钟有发送过
                Integer sendNumInfo = sendNumMap.get(nowDate);
                if (sendNumInfo <= workPlatSendNum) {
                    ctx.output(textMessageOutputTag,value);
                    log.info("the signal output to the FACTOR_TEXT_MESSAGE eventId:{}",value.getEventId());
                    sendNum++;
                    sendNumMap.put(nowDate,sendNum);
                }
            }else {
                sendNumMap.clear();
                ctx.output(textMessageOutputTag,value);
                log.info("the signal output to the FACTOR_TEXT_MESSAGE eventId:{}", value.getEventId());
                sendNum = 1;
                sendNumMap.put(nowDate,sendNum);
            }
        }
    }

    /**
     * 过滤判断src时间是否在实例推送区间
     */
    private boolean filterCompareSrcTime(String srcTimestamp, FxTaTradeSignalOutputEvent.SignalDetailResult eventData) {
        Date srcTime = DateUtil.stringToDate(srcTimestamp, DateUtil.DT_FORMAT_PATTERN);
        Date pushEndTime = DateUtil.workTimeTra(eventData.getPushEndTime());
        Date pushStartTime = DateUtil.workTimeTra(eventData.getPushStartTime());
        return pushEndTime.compareTo(srcTime)>0 && pushStartTime.compareTo(srcTime)<0;
    }
}
