package com.nbcb.factor.filter;

import com.nbcb.factor.enums.MarketEnum;
import com.nbcb.factor.enums.PriceTypeEnum;
import com.nbcb.factor.event.broker.BrokerBondDeal;
import lombok.extern.slf4j.Slf4j;

/**
 * broker成交行情过滤
 */
@Slf4j
public class BrokerBondDealFilter {
    /**
     * 成交行情过滤逻辑
     */
    public static BrokerBondDeal brokerBondDealFilter(BrokerBondDeal bondDeal) {
        //非银行间市场直接丢弃
        if (!bondDeal.getMarket().equals(MarketEnum.CFETS.getMarket())) {
            log.info("BrokerBondDealFilter filter GlobalId:{}" +
                    ",Reason for filter is market-交易市场 not cfets!",bondDeal.getGlobalId());
            return null;
        }
        //成交状态F表示正常
        if (!"F".equals(bondDeal.getExecType())) {
            log.info("BrokerBondDealFilter filter GlobalId:{}" +
                            ",Reason for filter is ExecType-成交状态 not F-正常！"
                    ,bondDeal.getGlobalId());
            return null;
        }
        String priceType = bondDeal.getDealPriceType();
        //只保留YIELD和到期收益率
        if (!PriceTypeEnum.YIELD.getCode().equals(priceType)
                && !(PriceTypeEnum.MATURITY.getCode().equals(bondDeal.getDealPriceType()))) {
            log.info("BrokerBondDealFilter filter GlobalId:{}," +
                    "Reason fro filter is priceType-成交价格类别 not 9-收益率" +
                    "or DealYieldType-成交收益率类型 not MATURITY-到期收益率！",
                    bondDeal.getGlobalId());
            return null;
        }
        return bondDeal;
    }
}
