package com.nbcb.factor.web.job.calc.impl;

import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.event.MetricsRegistry;
import com.nbcb.factor.event.MonitorableDTO;
import com.nbcb.factor.event.StrategyCommand;
import com.nbcb.factor.web.job.basic.impl.RidingYieldCurveSynJob;
import com.nbcb.factor.web.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时发送骑乘reload消息到kafka
 *
 * @author k11866
 * @date 2023/6/16 14:38
 */
@Component
public class RidingKafkaCmdJob {
    public static final Logger log = LoggerFactory.getLogger(RidingKafkaCmdJob.class);
   private static final String RELOAD = "reload";
   private static final String RIDING_YIELD_CURVE = "RidingYieldCurve";
   private static final String ALL = "ALL";

   /**
    * Kafka推送消息service
    */
   @Autowired
   private RidingYieldCurveSynJob ridingYieldCurveSynJob;

   @Scheduled(cron = "0 0/5 8-20  * * ?")
   public void sendRidingKafkaCmdJob() {
       // 初始化发送对象
       MonitorableDTO<StrategyCommand> sendEntity = new MonitorableDTO<>();
       // 初始化命令对象
       StrategyCommand strategyCommand = new StrategyCommand();
       strategyCommand.setCmd(RELOAD); // 因子实例ID
       strategyCommand.setStrategyInstanceId("");
       strategyCommand.setOperator(""); // 操作人
       strategyCommand.setCmdTime(String.valueOf(DateUtils.getCurrTime())); // 命令时间
       strategyCommand.setStrategyName(RIDING_YIELD_CURVE); // 模型名称
       strategyCommand.setIndexCategory(""); // 指标名称
       strategyCommand.setCmdType(ALL); // 全部计算/单个计算
       sendEntity.setContent(strategyCommand);
       sendEntity.setMetricsRegistry(new MetricsRegistry());
       try {
           log.info("定时骑乘计算开始！");
           this.ridingYieldCurveSynJob.executeJob(JsonUtil.toJson(sendEntity));
           log.info("定时骑乘计算完成！");
       } catch (Exception e) {
           log.info("定时计算骑乘异常!异常为:{}", e);
       }
   }
}