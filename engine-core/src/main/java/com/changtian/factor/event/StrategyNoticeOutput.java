package com.changtian.factor.event;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.entity.riding.RidingAssetPool;
import com.changtian.factor.entity.riding.RidingStrategyInstance;
import com.changtian.factor.enums.DataProcessNoticeTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知类
 */
@Setter@Getter@ToString
public class StrategyNoticeOutput implements OutputEvent, Serializable {
    private static final long serialVersionUID = 1L;
    public static final  String TOPIC = "FACTOR_ANALYZE_STRATEGY_NOTICE";
    private static final String KEY = MethodHandles.lookup().lookupClass().getSimpleName();
    private final long createdTimeStamp = System.currentTimeMillis();

    private String eventId;//时间id
    private String eventName;//时间名称
    private String notice = "finish";//命令
    private String  key ;
    private String noticeTime;//通知时间
    private String strategyName;//策略名称
    //通知类型 BASIC_DATA_PROCESS 基础数据处理 RIDING_YIELD_JOB 骑乘job通知 默认为骑乘通知
    private String noticeType = DataProcessNoticeTypeEnum.RIDING_YIELD_JOB.getKey();
    private List<StrategyNoticeDetail> data = new ArrayList<>();//跑完的实例集合

    public StrategyNoticeOutput(List<RidingAssetPool> voList){
        this.key = KEY;
        this.noticeTime = DateUtil.getNowstr();
        for (RidingAssetPool ra : voList) {
            this.strategyName = ra.getStrategyName();
            if (CollectionUtils.isEmpty(ra.getStrategyInstanceVoList())) {
                continue;
            }
            for (RidingStrategyInstance instance : ra.getStrategyInstanceVoList()) {
                StrategyNoticeDetail detail = new StrategyNoticeDetail();
                detail.setDefinitionId(ra.getStrategyId());
                detail.setDefinitionName(ra.getStrategyName());
                detail.setInstanceId(instance.getInstanceId());
                detail.setInstanceName(instance.getInstanceName());
                detail.setAssetPoolId(ra.getAssetPoolId());
                detail.setAssetPoolName(ra.getAssetPoolName());
                this.data.add(detail);
            }
        }
    }
    @Override
    public String getEventId(){return eventId;}
    @Override
    public String getEventName(){return eventName;}

    @Override
    public String getSrcTimestamp() {
        return null;
    }

    @Override
    public String getResTimestamp() {
        return null;
    }

    @Override
    public String getResultType() {
        return STRATEGY_NOTICE;
    }

    @Override
    public String getCreateTime() {
        return null;
    }

    @Override
    public String getUpdateTime() {
        return null;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public String getPkKey() {
        return null;
    }

    @Override
    public String getCompressFlag() {
        return null;
    }

    @Override
    public <T> T getEventData() {
        return null;
    }

    @Override
    public String getInstrumentId() {
        return null;
    }
}
