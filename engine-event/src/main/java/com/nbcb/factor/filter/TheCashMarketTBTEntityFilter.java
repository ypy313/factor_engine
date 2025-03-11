package com.nbcb.factor.filter;

import com.nbcb.factor.event.cmds.TheCashMarketTBTEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述：cmds成交行情过滤
 */
@Slf4j
public class TheCashMarketTBTEntityFilter {
    public static TheCashMarketTBTEntity theCashMarketTBTEntityFilter(TheCashMarketTBTEntity marketTBT){
        //只留交易方式为3-匿名点击  5-点击成交 的成交
        if (!(marketTBT.getTradeMethod().equals("3")|| marketTBT.getTradeMethod().equals("5"))) {
            log.info("TheCashMarketTBTEntityFilter filter GlobalId:{}" +
                    "Reason for filter is TradeMethod-交易方式 is not 3-匿名点击 or not 5-点击成交",
                    marketTBT.getGlobalId());
            return null;
        }
        return marketTBT;
    }
}

