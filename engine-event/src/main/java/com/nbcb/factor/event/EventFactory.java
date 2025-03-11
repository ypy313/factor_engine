package com.nbcb.factor.event;

import cn.hutool.json.JSONUtil;
import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.enums.DataProcessNoticeTypeEnum;
import com.nbcb.factor.event.broker.BrokerBondBestOffer;
import com.nbcb.factor.event.broker.BrokerBondDeal;
import com.nbcb.factor.event.cfets.MarketDataSnapshotFullRefresh;
import com.nbcb.factor.event.cmds.TheCashMarketTBTEntity;
import com.nbcb.factor.event.forex.FactorFxPmAllMarketData;
import com.nbcb.factor.event.pm.FactorPreciousMetalMarketData;
import com.nbcb.factor.event.spread.AllMarketData;
import com.nbcb.factor.filter.BrokerBondBestOfferFilter;
import com.nbcb.factor.filter.BrokerBondDealFilter;
import com.nbcb.factor.filter.MarketDataSnapshotFullRefreshFilter;
import com.nbcb.factor.filter.TheCashMarketTBTEntityFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Map;

/**
 * 事件工厂
 */
@Slf4j
public class EventFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static String SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH = "SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH";
    private static String SMDS_IN_CDH_BOND_DEAL = "SMDS_IN_CDH_BOND_DEAL";
    private static String FACTOR_BROKER_BOND_BEST_OFFER = "FACTOR_BROKER_BOND_BEST_OFFER";
    private static String FACTOR_ANALYZE_STRATEGY_CMD = "FACTOR_ANALYZE_STRATEGY_CMD";
    private static String FACTOR_BROKER_BOND_DEAL ="FACTOR_BROKER_BOND_DEAL";
    private static final String SMDS_IN_CFETS_CMDS_BOND_DEAL = "SMDS_IN_CFETS_CMDS_BOND_DEAL";
    /**
     * 消息创建工厂
     */
    public static BondInputEvent create(String topic , String value){
        if (StringUtils.equals(topic,SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH)){
            MarketDataSnapshotFullRefreshBondInputEvent event = new MarketDataSnapshotFullRefreshBondInputEvent();
            MonitorableDTO dto = JSONUtil.toBean(value, MonitorableDTO.class);
            MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh =
                    JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),MarketDataSnapshotFullRefresh.class);
            marketDataSnapshotFullRefresh = MarketDataSnapshotFullRefreshFilter.mdSnapshotFilter(marketDataSnapshotFullRefresh);
            if (marketDataSnapshotFullRefresh == null) {
                log.info("marketDataSnapshotFullRefresh filter GlobalId :{},marketDataSnapshotFullRefresh {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
            }

            event.setInstrumentId(marketDataSnapshotFullRefresh.getSecurityID());
            event.setEventData(marketDataSnapshotFullRefresh);
             return event;
        }
        if (StringUtils.equals(topic,FACTOR_BROKER_BOND_BEST_OFFER)) {
            BrokerBondBestOfferBondInputEvent event = new BrokerBondBestOfferBondInputEvent();
            MonitorableDTO dto = JSONUtil.toBean(value, MonitorableDTO.class);
            BrokerBondBestOffer brokerBondBestOffer = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), BrokerBondBestOffer.class);
            brokerBondBestOffer = BrokerBondBestOfferFilter.brokerBondBestOfferFilter(brokerBondBestOffer);

            if(brokerBondBestOffer == null){
                log.info("BrokerBondBestOfferFilter filter GlobalId : {},brokerBondBestOffer {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;//数据过滤
            }
            event.setInstrumentId(brokerBondBestOffer.getSecurityID());
            event.setEventData(brokerBondBestOffer);
            return event;
        }
        if (StringUtils.equals(topic,FACTOR_BROKER_BOND_DEAL)){
            BrokerBondDealBondInputEvent event = new BrokerBondDealBondInputEvent();
            MonitorableDTO dto = JSONUtil.toBean(value, MonitorableDTO.class);
            BrokerBondDeal brokerBondDeal = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), BrokerBondDeal.class);
            brokerBondDeal = BrokerBondDealFilter.brokerBondDealFilter(brokerBondDeal);
            if (brokerBondDeal == null){
                LOGGER.info("BrokerBondDeal filter GlobalId : {},brokerBondDeal {} ",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return  null;//数据过滤
            }
            event.setInstrumentId(brokerBondDeal.getSecurityID());
            event.setEventData(brokerBondDeal);
            return event;
        }
        if (StringUtils.equals(topic, SMDS_IN_CFETS_CMDS_BOND_DEAL)){
            CfetsCmdsBondDealInputEvent event = new CfetsCmdsBondDealInputEvent();
            MonitorableDTO dto = JSONUtil.toBean(value, MonitorableDTO.class);
            TheCashMarketTBTEntity theCashMarketTBTEntity = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),TheCashMarketTBTEntity.class);
            theCashMarketTBTEntity = TheCashMarketTBTEntityFilter.theCashMarketTBTEntityFilter(theCashMarketTBTEntity);
            if (theCashMarketTBTEntity == null) {
                log.info("TheCashMarketTBTEntityFilter filter GlobalId :{},theCashMarketTBTEntity {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }

            event.setInstrumentId(theCashMarketTBTEntity.getSecurityID());
            event.setEventData(theCashMarketTBTEntity);
            return event;
        }

        if (StringUtils.equals(topic,FACTOR_ANALYZE_STRATEGY_CMD)){
            StrategyCommandBondInputEvent event = new StrategyCommandBondInputEvent();
            MonitorableDTO dto = JSONUtil.toBean(value, MonitorableDTO.class);
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }
        LOGGER.error("can't create event");
        return null;
    }

    public static BondInputEvent createMarketProcessing(String eventStr){
        MonitorableDTO dto = JSONUtil.toBean(eventStr,MonitorableDTO.class);
        //通知为特殊情况 独立处理 通知类事件没有事件监控数据
        if(null == dto || null == dto.getMetricsRegistry()){
            log.error("字符串:{},为通知类事件数据！",eventStr);
            StrategyNoticeInputEvent inputEvent = JSONUtil.toBean(JSONUtil.toJsonStr(eventStr),StrategyNoticeInputEvent.class);
            if (null != inputEvent
                    && DataProcessNoticeTypeEnum.BASIC_DATA_PROCESS.getKey().equals(inputEvent.getNoticeType())) {
                return inputEvent;
            }
            log.info("非基础数据处理完成时间通知不进行处理:{}",eventStr);
            return null;
        }
        Map map = (Map) dto.getContent();
        String sendingTime = (String) map.get("sendingTime");
        if (StringUtils.isBlank(sendingTime)) {
            sendingTime = DateUtil.getSendTimeStr();
        }
        String topic = (String) map.get("topic");
        if (StringUtils.equals(topic,SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH)) {
            MarketDataSnapshotFullRefreshBondInputEvent event = new MarketDataSnapshotFullRefreshBondInputEvent();
            MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh = JSONUtil
                    .toBean(JSONUtil.toJsonStr(dto.getContent()),MarketDataSnapshotFullRefresh.class);
            marketDataSnapshotFullRefresh.setGlobalId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            marketDataSnapshotFullRefresh = MarketDataSnapshotFullRefreshFilter.mdSnapshotFilter(marketDataSnapshotFullRefresh);
            if (marketDataSnapshotFullRefresh == null) {
                log.info("marketDataSnapshotFullRefresh filter GlobalId:{},marketDataSnapshotFullRefresh {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;//数据过滤
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(marketDataSnapshotFullRefresh.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(marketDataSnapshotFullRefresh);
            return event;
        } else if(StringUtils.equals(topic,FACTOR_BROKER_BOND_BEST_OFFER)){
            BrokerBondBestOfferBondInputEvent event = new BrokerBondBestOfferBondInputEvent();
            BrokerBondBestOffer bondBestOffer = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),BrokerBondBestOffer.class);
            bondBestOffer.setGlobalId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            bondBestOffer = BrokerBondBestOfferFilter.brokerBondBestOfferFilter(bondBestOffer);
            if (bondBestOffer == null) {
                log.info("BrokerBondBestOfferBond filter {} GlobalId :{} ,bondBestOffer {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()),"");
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(bondBestOffer.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(bondBestOffer);
            return event;
        }else if(StringUtils.equals(topic,FACTOR_ANALYZE_STRATEGY_CMD)){
            StrategyCommandBondInputEvent event = new StrategyCommandBondInputEvent();
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }else if(StringUtils.equals(topic,FACTOR_BROKER_BOND_DEAL)){
            BrokerBondDealBondInputEvent event = new BrokerBondDealBondInputEvent();
            BrokerBondDeal brokerBondDeal = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), BrokerBondDeal.class);
            brokerBondDeal.setGlobalId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            brokerBondDeal = BrokerBondDealFilter.brokerBondDealFilter(brokerBondDeal);
            if (brokerBondDeal == null) {
                log.info("BrokerBondDealFilter filter GlobalId:{},brokerBondDeal {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(brokerBondDeal.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(brokerBondDeal);
            return event;
        }else if(StringUtils.equals(topic,SMDS_IN_CFETS_CMDS_BOND_DEAL)){
            CfetsCmdsBondDealInputEvent event = new CfetsCmdsBondDealInputEvent();
            TheCashMarketTBTEntity theCashMarketTBTEntity = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),TheCashMarketTBTEntity.class);
            theCashMarketTBTEntity.setGlobalId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            theCashMarketTBTEntity = TheCashMarketTBTEntityFilter.theCashMarketTBTEntityFilter(theCashMarketTBTEntity);
            if (theCashMarketTBTEntity == null) {
                log.info("TheCashMarketTBTEntityFilter filter GlobalId:{},TheCashMarketTBTEntity {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(theCashMarketTBTEntity.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(theCashMarketTBTEntity);
            return event;
        }else {
            log.error("can`t create event");
        }
        return null;
    }

    public static BondInputEvent create(String value){
        MonitorableDTO dto = JSONUtil.toBean(value,MonitorableDTO.class);
        Map map = (Map) dto.getContent();
        String sendingTime = (String) map.get("sendingTime");
        if (StringUtils.isBlank(sendingTime)) {
            sendingTime = DateUtil.getSendTimeStr();
        }
        String topic = (String) map.get("topic");
        if(StringUtils.equals(topic,SMDS_IN_CFETS_MARKET_DATA_SNAPSHOT_FULL_REFRESH)){
            MarketDataSnapshotFullRefreshBondInputEvent event = new MarketDataSnapshotFullRefreshBondInputEvent();
            MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh = JSONUtil
                    .toBean(JSONUtil.toJsonStr(dto.getContent()), MarketDataSnapshotFullRefresh.class);

            marketDataSnapshotFullRefresh = MarketDataSnapshotFullRefreshFilter.mdSnapshotFilter(marketDataSnapshotFullRefresh);
            if (marketDataSnapshotFullRefresh == null) {
                log.info("marketDataSnapshotFullRefresh filter GlobalId:{},marketDataSnapshotFullRefresh {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(marketDataSnapshotFullRefresh.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(marketDataSnapshotFullRefresh);
            return event;
        }
        if(StringUtils.equals(topic,FACTOR_BROKER_BOND_BEST_OFFER)){
            BrokerBondBestOfferBondInputEvent event = new BrokerBondBestOfferBondInputEvent();
            BrokerBondBestOffer brokerBondBestOffer = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),BrokerBondBestOffer.class);
            brokerBondBestOffer = BrokerBondBestOfferFilter.brokerBondBestOfferFilter(brokerBondBestOffer);
            if(brokerBondBestOffer == null){
                log.info("BrokerBondBestOfferFilter filter GlobalId:{},brokerBondBestOffer{}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(brokerBondBestOffer.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(brokerBondBestOffer);
            return event;
        }
        if(StringUtils.equals(topic,FACTOR_ANALYZE_STRATEGY_CMD)){
            StrategyCommandBondInputEvent event = new StrategyCommandBondInputEvent();
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }
        if(StringUtils.equals(topic,FACTOR_BROKER_BOND_DEAL)){
            BrokerBondDealBondInputEvent event = new BrokerBondDealBondInputEvent();
            BrokerBondDeal brokerBondDeal = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), BrokerBondDeal.class);
            brokerBondDeal = BrokerBondDealFilter.brokerBondDealFilter(brokerBondDeal);
            if (brokerBondDeal == null) {
                log.info("BrokerBondDealFilter filter GlobalId:{},brokerBondBestOffer {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(brokerBondDeal.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(brokerBondDeal);
            return event;
        }
        if(StringUtils.equals(topic,SMDS_IN_CFETS_CMDS_BOND_DEAL)){
            CfetsCmdsBondDealInputEvent event = new CfetsCmdsBondDealInputEvent();
            TheCashMarketTBTEntity theCashMarketTBTEntity = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),TheCashMarketTBTEntity.class);
            theCashMarketTBTEntity = TheCashMarketTBTEntityFilter.theCashMarketTBTEntityFilter(theCashMarketTBTEntity);
            if (theCashMarketTBTEntity == null) {
                log.info("TheCashMarketTBTEntityFilter filter GlobalId:{},TheCashMarketTBTEntity {}",
                        dto.getMetricsRegistry().getGlobalId(),JSONUtil.toJsonStr(dto.getContent()));
                return null;
            }
            event.setEventId(String.valueOf(dto.getMetricsRegistry().getGlobalId()));
            event.setInstrumentId(theCashMarketTBTEntity.getSecurityID());
            event.setCreateTimestamp(sendingTime);
            event.setEventData(theCashMarketTBTEntity);
            return event;
        }
        log.error("can`t create event");
        return null;
    }

    public static BondInputEvent createSpread(String value){
        MonitorableDTO dto = JSONUtil.toBean(value,MonitorableDTO.class);
        //融合行情为特殊情况 独立处理
        if (null != dto && null ==dto.getContent()) {
            MarketDataSpreadBondInputEvent event = new MarketDataSpreadBondInputEvent();
            AllMarketData allMarketData = JSONUtil
                    .toBean(JSONUtil.toJsonStr(value),AllMarketData.class);
            if(allMarketData == null){
                log.info("allMarketData 数据为空");
                return null;
            }
            allMarketData.setGlobalId(allMarketData.getEventId());
            event.setInstrumentId(allMarketData.getInstrumentId());
            event.setEventData(allMarketData);
            return event;
        }
        Map map = (Map) dto.getContent();
        String topic = (String) map.get("topic");
        if(StringUtils.equals(topic,FACTOR_ANALYZE_STRATEGY_CMD)){
            StrategyCommandBondInputEvent event = new StrategyCommandBondInputEvent();
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()), StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }else {
            log.error("Spread 暂不支持事件创建：{}，的消息：{}！",topic,value);
        }
        return null;
    }

    public static SymbolInputEvent createFx(String topic,String message){
        if(StrategyCommandSymbolInputEvent.TOPIC.equals(topic)){
            //命令
            MonitorableDTO dto = JSONUtil.toBean(message,MonitorableDTO.class);
            StrategyCommandSymbolInputEvent event = new StrategyCommandSymbolInputEvent();
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }else if(FactorFxReuterMarketDataEvent.TOPIC.equals(topic)||FactorFxReuterMarketDataEvent.TOPIC_BLOOMBERG.equals(topic)){
            FactorFxReuterMarketDataEvent event = new FactorFxReuterMarketDataEvent();
            FactorFxPmAllMarketData marketData = JSONUtil.toBean(message, FactorFxPmAllMarketData.class);
            if(marketData == null){
                log.info("factorFxReuterMarketDataEvent is null ! topic:{} ,message:{}",topic,message);
                return null;
            }
            event.setEventId(marketData.getEventId());
            event.addRelationId("_");
            event.setCreateTimestamp(marketData.getCreated());
            event.setEventData(marketData);
            return event;
        }else {
            log.info("暂时不支持当前topic:{},的消息！",topic);
        }
        return null;
    }

    public static InputEvent createPreciousMetalEvent(String topic,String message){
        if(StrategyCommandSymbolInputEvent.TOPIC.equals(topic)){
            //命令
            MonitorableDTO<?> dto = JSONUtil.toBean(message,MonitorableDTO.class);
            StrategyCommandSymbolInputEvent event = new StrategyCommandSymbolInputEvent();
            StrategyCommand strategyCommand = JSONUtil.toBean(JSONUtil.toJsonStr(dto.getContent()),StrategyCommand.class);
            event.setInstrumentId(strategyCommand.getCmd());
            event.setEventData(strategyCommand);
            return event;
        }else if(FactorPreciousMetalMetalMarketDataEvent.TOPIC.equals(topic)){
            FactorPreciousMetalMetalMarketDataEvent event = new FactorPreciousMetalMetalMarketDataEvent();
            FactorPreciousMetalMarketData marketData = JSONUtil.toBean(message, FactorPreciousMetalMarketData.class);
            if(marketData == null){
                log.info("factorPreciousMetalMetalMarketData is null ! topic:{} ,message:{}",topic,message);
                return null;
            }
            event.setEventData(marketData);
            event.setSymbol();
            event.setEventId(marketData.getEventId());
            event.addRelationId("_");
            long time = new Date().getTime();
            event.setCreateTimestamp(String.valueOf(time));
            return event;
        }else if(StrategyNoticeInputEvent.TOPIC.equals(topic)){
            StrategyNoticeInputEvent inputEvent = JSONUtil.toBean(message,StrategyNoticeInputEvent.class);
            log.info("createRiding 接收到topic:{}的数据，为通知类事件数据，noticeType为：{}！",topic,inputEvent.getNoticeType());
            if(null !=inputEvent
            && DataProcessNoticeTypeEnum.HOLIDAY_DATE.getKey().equals(inputEvent.getNoticeType())){
                return inputEvent;
            }
            return null;
        }else {
            log.info("暂时不支持当前topic:{}的消息!",topic);
        }
        return null;
    }
}
