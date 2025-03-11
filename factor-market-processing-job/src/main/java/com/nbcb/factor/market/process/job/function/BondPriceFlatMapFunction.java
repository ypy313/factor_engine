package com.nbcb.factor.market.process.job.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.entity.BondConfigEntity;
import com.nbcb.factor.enums.BusinessEnum;
import com.nbcb.factor.enums.SystemEnum;
import com.nbcb.factor.event.OutputEvent;
import com.nbcb.factor.output.MarketProcessingOutPut;
import com.nbcb.factor.output.bondprice.BondPriceOutputResult;
import com.nbcb.factor.output.bondprice.BondPriceResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 现价排序点差计算方法
 */
@Slf4j
public class BondPriceFlatMapFunction extends ProcessFunction<OutputEvent,OutputEvent> {
    private RedisUtil redisUtil;
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.redisUtil = RedisUtil.getInstance();
    }

    @Override
    public void processElement(OutputEvent value, ProcessFunction<OutputEvent, OutputEvent>.Context ctx, Collector<OutputEvent> out) throws Exception {
        MarketProcessingOutPut marketProcessingOutPut = (MarketProcessingOutPut) value;
        //点差等实时计算，且存储进入redis
        BondPriceOutputResult bondPriceOutputResult = calculationRealTime(marketProcessingOutPut);
        //kafka输出形式组装
        BondPriceResultEvent bondPriceResultEvent = new BondPriceResultEvent(marketProcessingOutPut);
        bondPriceResultEvent.setEventData(bondPriceOutputResult);
        out.collect(bondPriceResultEvent);
    }

    public BondPriceOutputResult calculationRealTime(MarketProcessingOutPut marketProcessingOutPut) {
        String securityId = marketProcessingOutPut.getInstrumentId();
        try{
            String commonKey = "";
            String currentPriceKey = "";
            BondConfigEntity bondConfigEntity = null;
            String mapperName = marketProcessingOutPut.getMapperName();
            if (StringUtils.isNotEmpty(mapperName)) {
                commonKey = SystemEnum.FACTOR.getKey()+"_"+ BusinessEnum.COMMON.getKey()
                        +"_"+mapperName;
                currentPriceKey = SystemEnum.FACTOR.getKey()+"_"+BusinessEnum.CURRENT_PRICE.getKey()
                        +"_"+mapperName;
            }
            //静态变量数据 key前缀改为
            String bondConfigEntityStr = redisUtil.getString(commonKey, securityId);
            if (StringUtils.isNotEmpty(bondConfigEntityStr)) {
                bondConfigEntity = JsonUtil.toObject(bondConfigEntityStr, BondConfigEntity.class);
            }
            //发送至新kafka topic数据
            BondPriceOutputResult bondPriceOutputResult = new BondPriceOutputResult();
            if (bondConfigEntity != null) {
                bondPriceOutputResult.setValuation(StringUtils.isNotEmpty(bondConfigEntity.getValuation())
                ? new BigDecimal(bondConfigEntity.getValuation()):null);
                BeanUtils.copyProperties(bondConfigEntity,bondPriceOutputResult);
            }
            //最新实时行情赋值
            marketProcessingOutPut.convertBondPriceResult(bondPriceOutputResult);
            //最新实时行情赋值
            marketProcessingOutPut.convertBondPriceResult(bondPriceOutputResult);
            //点差计算
            if (bondPriceOutputResult.getWholeBestBidYield() !=null
                    && bondPriceOutputResult.getWholeBestOfrYield() !=null) {
                BigDecimal spread = (bondPriceOutputResult.getWholeBestBidYield()
                        .subtract(bondPriceOutputResult.getWholeBestOfrYield()))
                        .multiply(BigDecimal.valueOf(100));
            }

            if (bondPriceOutputResult.getWholeBestBidYield() !=null) {
                if (bondPriceOutputResult.getLatestTrans() !=null) {
                    //最新成交相关计算
                    BigDecimal bidTrasDiff = bondPriceOutputResult.getWholeBestBidYield()
                            .subtract(bondPriceOutputResult.getLatestTrans()).multiply(BigDecimal.valueOf(100));
                    bondPriceOutputResult.setBidTransDiff(bidTrasDiff);
                }
                if(bondPriceOutputResult.getValuation() != null){
                    //中债估值相关计算
                    BigDecimal bidValuationDiff = bondPriceOutputResult.getWholeBestBidYield()
                            .subtract(bondPriceOutputResult.getValuation()).multiply(BigDecimal.valueOf(100));
                    bondPriceOutputResult.setBidValuationDiff(bidValuationDiff);
                }
            }

            if(bondPriceOutputResult.getWholeBestOfrYield() != null){
                if (bondPriceOutputResult.getLatestTrans() !=null) {
                    //最新成交相关计算
                    BigDecimal ofrTransDiff = bondPriceOutputResult.getLatestTrans()
                            .subtract(bondPriceOutputResult.getWholeBestOfrYield())
                            .multiply(BigDecimal.valueOf(100));
                }
                if(bondPriceOutputResult.getValuation() != null){
                    //中债估值
                    BigDecimal ofrValuationDiff = bondPriceOutputResult.getValuation()
                            .subtract(bondPriceOutputResult.getWholeBestOfrYield())
                            .multiply(BigDecimal.valueOf(100));
                    bondPriceOutputResult.setOfrValuationDiff(ofrValuationDiff);
                }
            }
            //更新点差redis
            redisUtil.setString(currentPriceKey,securityId,JsonUtil.safeToJson(bondPriceOutputResult));
            return bondPriceOutputResult;
        }catch (JsonProcessingException e) {
            log.error("现价排序String -> RdiSecurityValuation.class失败，原因为：{}",e.getMessage(),e);
        }
        return null;
    }
}
