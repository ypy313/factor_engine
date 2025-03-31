package com.nbcb.factor.output;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.entity.riding.RidingAssetPool;
import com.nbcb.factor.event.OutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class RidingBondRankingOutputEvent implements OutputEvent, Serializable {
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
    private String showHoldPeriod;//1M 3M 6M 9M 1Y CUSTOM
    private String showFunsRate;//Shibor7D repo7D
    private String algorithm;//estimate-估算  actuary-精算
    private String curveChange;//noChange-不变 upper-向上 down-向下 custom-自定义
    private String futureDeviation;//all-全部  currentDeviation-使用当前偏离 custom-自定义
    private String interRate;//3-最新值  9-过去均值  8-自定义
    private List<RidingBondRankingDetailResult> eventData = new ArrayList<>();

    public RidingBondRankingOutputEvent(RidingAssetPool ridingAssetPool) {
        this.eventId = ridingAssetPool.getStrategyName()+"_"+ridingAssetPool.getAssetPoolId();
        this.eventName = ridingAssetPool.getStrategyName();
        this.srcTimestamp = DateUtil.getSendingTime();
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

    @Override
    public String getInstrumentId() {
        return "";
    }
}
