package com.nbcb.factor.web.job.basic.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.enums.DataProcessNoticeTypeEnum;
import com.nbcb.factor.event.StrategyNoticeOutput;
import com.nbcb.factor.web.entity.PmTpHoliday;
import com.nbcb.factor.web.job.basic.FactorBasicJob;
import com.nbcb.factor.web.kafka.service.KafkaMessageSendService;
import com.nbcb.factor.web.service.HolidayService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import scala.util.Try;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 贵金属节假日数据
 */
@Component
public class PmHolidaySynJob implements FactorBasicJob {
    /**
     * kafak推送消息service
     */
    @Autowired
    private KafkaMessageSendService kafkaMessageSendService;
    @Autowired
    private HolidayService holidayService;
    private final RedisUtil redisUtil = RedisUtil.getInstance();

    public ReturnT executeJob(Object param) {
        XxlJobLogger.log("G贵金属日历跑批存储开始！");
        List<PmTpHoliday> pmTpHolidays = holidayService.selectPmHolidayByDate();
        List<String> pmHoliday = pmTpHolidays.stream().map(e -> e.getHoliday()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pmHoliday)) {
            try {
                XxlJobLogger.log("贵金属日历跑批redis存储开始！");
                redisUtil.setRiding(AllRedisConstants.FACTOR_PM_HOLIDAY,
                        JsonUtil.toJson(pmHoliday));
                XxlJobLogger.log("贵金属日历跑批redis存储redis存储结束！");
                pushCmdToKafka();
            }catch (JsonProcessingException e){
                e.printStackTrace();
                return ReturnT.FAIL;
            }
        }
        XxlJobLogger.log("贵金属日历跑批存储结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 推送完成命令到kafka
     */
    private void pushCmdToKafka() {
        //初始化命令对象
        StrategyNoticeOutput output = new StrategyNoticeOutput(new ArrayList<>());
        output.setNoticeType(DataProcessNoticeTypeEnum.HOLIDAY_DATE.getKey());
        try {
            XxlJobLogger.log("定时基础数据处理完成消息推送！{}", JSONUtil.toJsonStr(output));
            this.kafkaMessageSendService.sendMessage(StrategyNoticeOutput.TOPIC,JSONUtil.toJsonStr(output));
            XxlJobLogger.log("定时基础数据处理完成消息推送完成！");
        }catch (Exception e){
            XxlJobLogger.log("定时基础数据处理完成消息异常：{}",e.getMessage());
        }
    }
}
