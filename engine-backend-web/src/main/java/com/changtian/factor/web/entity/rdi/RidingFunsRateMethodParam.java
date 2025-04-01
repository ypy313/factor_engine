package com.changtian.factor.web.entity.rdi;

import com.changtian.factor.entity.riding.BondCF;
import com.changtian.factor.entity.riding.CbondCurveCnbd;
import com.changtian.factor.entity.riding.RidingAssetPool;
import com.changtian.factor.entity.riding.RidingStrategyInstance;
import com.changtian.factor.output.RidingBondRankingDetailResult;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 骑乘请求方法参数
 */
@Getter
@Setter
public class RidingFunsRateMethodParam {
    private List<RidingAssetPool> voList;
    private List<CbondCurveCnbd> curvecnbdList;
    private String settlementDate;
    private String algorithmStr;
    private RidingAssetPool ridingAssetPool;
    private String futureSettlementDate;
    private List<RidingBondRankingDetailResult> eventData;
    private RidingStrategyInstance strategyInstance;
    private RidingBondRankingDetailResult result;
    private String fundsRateKey;
    private String interestRateValue;
    private List<BondCF> bondcFList;
    private String type;
    private BigDecimal currentValuationYield;
    private int days;
    private int nowDays;

    public RidingFunsRateMethodParam convertOne(RidingFunsRateMethodParam param, List<RidingAssetPool> voList
            , List<CbondCurveCnbd> curvecnbdList,
                                                String settlementDate, String algorithmStr) {
        param.setVoList(voList);
        param.setCurvecnbdList(curvecnbdList);
        param.setSettlementDate(settlementDate);
        param.setAlgorithmStr(algorithmStr);
        return param;
    }

    public RidingFunsRateMethodParam convertTwo(RidingFunsRateMethodParam param
            , RidingAssetPool ridingAssetPool,
                                                String futureSettlementDate,
                                                List<RidingBondRankingDetailResult> eventData,
                                                RidingStrategyInstance strategyInstance) {
        param.setFutureSettlementDate(futureSettlementDate);
        param.setEventData(eventData);
        param.setStrategyInstance(strategyInstance);
        param.setRidingAssetPool(ridingAssetPool);
        return param;
    }

    public RidingFunsRateMethodParam convertThree(RidingFunsRateMethodParam param,
                                                  RidingBondRankingDetailResult result,
                                                  String fundsRatekey, String interestRateValue,
                                                  List<BondCF> bondcFList) {
        param.setResult(result);
        param.setFundsRateKey(fundsRatekey);
        param.setInterestRateValue(interestRateValue);
        param.setBondcFList(bondcFList);
        return param;
    }

    public RidingFunsRateMethodParam convertFour(RidingFunsRateMethodParam param, String type,
                                                 BigDecimal currentValuationYield, int days, int nowDays) {
        param.setType(type);
        param.setCurrentValuationYield(currentValuationYield);
        param.setDays(days);
        param.setNowDays(nowDays);
        return param;
    }

}
