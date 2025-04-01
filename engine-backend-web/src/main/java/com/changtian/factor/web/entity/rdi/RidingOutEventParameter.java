package com.changtian.factor.web.entity.rdi;

import com.changtian.factor.entity.riding.CbondCurveCnbd;
import com.changtian.factor.entity.riding.RidingAssetPool;
import com.changtian.factor.entity.riding.RidingStrategyInstance;
import com.changtian.factor.output.RidingBondRankingDetailResult;
import com.changtian.factor.output.RidingBondRankingOutputEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 骑乘方法调用参数
 */
@Getter
@Setter
@ToString
public class RidingOutEventParameter {
    private List<RidingAssetPool> voList;
    private List<CbondCurveCnbd> curveCnbdList;
    private String settlementDate;
    private String shp;
    private  String algorithmStr;
    private RidingAssetPool ridingAssetPool;
    private List<RidingStrategyInstance> strategyInstanceVoList;
    private String futureSettlementDate;
    private RidingBondRankingOutputEvent event;
    private List<RidingBondRankingDetailResult> eventDate;
}
