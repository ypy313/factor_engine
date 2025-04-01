package com.changtian.factor.filter;

import com.changtian.factor.event.cfets.MarketDataSnapshotFullRefresh;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * cfets 行情过滤器 只处理T+1行情
 */
@Slf4j
public class MarketDataSnapshotFullRefreshFilter {
    public static MarketDataSnapshotFullRefresh mdSnapshotFilter(MarketDataSnapshotFullRefresh mdSnapshot){
        List<MarketDataSnapshotFullRefresh.NoMDEntries> entriesList = mdSnapshot.getNoMDEntriesList();
        //为空
        if (entriesList==null || entriesList.isEmpty()) {
            log.info("marketDataSnapshotFullRefresh filter GlobalId:{},Reason for filter is entriesList is null",mdSnapshot.getGlobalId());
            return null;
        }
        MarketDataSnapshotFullRefresh.NoMDEntries entry = entriesList.get(0);//最优
        if (entry == null) {
            log.info("marketDataSnapshotFullRefresh filter GlobalId:{}" +
                    ",Reason for filter is entriesList.get(0) is null",mdSnapshot.getGlobalId());
            return null;
        }
        if (StringUtils.equals("XSWAP",mdSnapshot.getMsgType())) {
            return mdSnapshot;
        }else {
            String settlType = entry.getSettlType();
            log.info("MarketDataSnapshotFullRefresh entry settlType:{} {}",settlType,mdSnapshot.getSendingTime());
            if (StringUtils.equals("2",settlType)) {
                //T+1
                return mdSnapshot;
            }else {
                //不是T+1
                log.info("marketDataSnapshotFullRefresh" +
                        " filter globalId:{},Reason for filter is not T+1",mdSnapshot.getGlobalId());
                return null;
            }
        }
    }
}
