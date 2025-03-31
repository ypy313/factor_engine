package com.nbcb.factor.output;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.entity.riding.RidingAssetPool;
import com.nbcb.factor.event.OutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@ToString
public class RidingYieldCurveOutputEvent implements OutputEvent, Serializable {
    private String eventId;
    private String eventName;
    private String pkKey;
    private String srcTimestamp;
    private String resTimestamp;
    private String createTime;
    private String updateTime;
    private String definitionId;
    private String definitionName;
    private String assetPoolId;
    private String assetPoolName;
    private List<RidingYieldCurveDetailResult> eventData = new ArrayList<>();

    public RidingYieldCurveOutputEvent(RidingAssetPool ridingAssetPool){
        this.eventId = ridingAssetPool.getStrategyName()+"_"+ridingAssetPool.getAssetPoolId();
        this.eventName = ridingAssetPool.getStrategyName();
        this.srcTimestamp = DateUtil.getSendingTime();
        this.resTimestamp = DateUtil.getSendingTime();
        this.updateTime = DateUtil.getSendingTime();
        this.createTime = DateUtil.getSendingTime();
        this.setDefinitionId(ridingAssetPool.getStrategyId());
        this.setDefinitionName(ridingAssetPool.getStrategyName());
        this.setAssetPoolId(ridingAssetPool.getAssetPoolId());
        this.setAssetPoolName(ridingAssetPool.getAssetPoolName());
    }

    @Override
    public String getEventId() {
        return "";
    }

    @Override
    public String getInstrumentId() {
        return "";
    }

    @Override
    public String getResultType() {
        return "";
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public String getCompressFlag() {
        return "";
    }
}
