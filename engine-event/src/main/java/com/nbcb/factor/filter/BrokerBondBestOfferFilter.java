package com.nbcb.factor.filter;

import com.nbcb.factor.enums.MarketEnum;
import com.nbcb.factor.enums.PriceTypeEnum;
import com.nbcb.factor.event.broker.BrokerBondBestOffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerBondBestOfferFilter {
    /**
     * 最优报价过滤逻辑
     */
    public static BrokerBondBestOffer brokerBondBestOfferFilter(BrokerBondBestOffer brokerBondBestOffer){
        //非银行间市场直接丢弃
        if (!brokerBondBestOffer.getMarket().equals(MarketEnum.CFETS.getMarket())) {
            log.info("BrokerBondBestOfferBond filter GlobalId:{},Reason for filter " +
                            "is market-交易市场 not cfets!"
                    ,brokerBondBestOffer.getGlobalId());
            return null;
        }
        //1、当双边均为非空行情时，固定传2，对应方向下的字段有则传
        //2、当一边为空行情时，固定传1，空行情方向下的字段均不穿
        //3、当双边均为空行情时，不传该组件
        //4、当为意向报价时，价格不传，其他字段有则传
        Integer mdEntries = brokerBondBestOffer.getMdEntries();
        if (mdEntries == null) {
            return brokerBondBestOffer;
        }
        //处理bid
        boolean bidFlag = bidFilter(brokerBondBestOffer);
        //处理ask
        boolean askFlag = askFilter(brokerBondBestOffer);
        if (bidFlag && !askFlag) {
            brokerBondBestOffer.setBid(null);
            brokerBondBestOffer.setBidQty(null);
        }else if (!bidFlag && askFlag) {
            brokerBondBestOffer.setAsk(null);
            brokerBondBestOffer.setAskQty(null);
        }else if (bidFlag && askFlag){
          log.info("BrokerBondBestOfferBond filter GlobalId:{},Reason for filter is BidPriceType not is 9-收益率" +
                  "and BidYieldType not is MATURITY-到期收益率" +
                  " and askPriceType not is 9-收益率 and askYieldType not is MATURITY-到期收益率！"
          ,brokerBondBestOffer.getGlobalId());
          return null;
        }
        return brokerBondBestOffer;
    }

    /**
     * 处理bid 报买价格类别不为收益率或者到期收益率就被过滤
     */
    private static boolean bidFilter(BrokerBondBestOffer brokerBondBestOffer) {
        if (PriceTypeEnum.YIELD.getCode().equals(brokerBondBestOffer.getBidPriceType())
                && PriceTypeEnum.MATURITY.getCode().equals(brokerBondBestOffer.getBidYieldType())) {
            return false;
        }
        return true;
    }

    /**
     * 处理 报卖价类别不为收益率或者到期收益率就被过滤
     */
    private static boolean askFilter(BrokerBondBestOffer brokerBondBestOffer) {
        if (PriceTypeEnum.YIELD.getCode().equals(brokerBondBestOffer.getAskPriceType())
                && PriceTypeEnum.MATURITY.getCode().equals(brokerBondBestOffer.getAskYieldType())) {
            return false;
        }
        return true;
    }
}
