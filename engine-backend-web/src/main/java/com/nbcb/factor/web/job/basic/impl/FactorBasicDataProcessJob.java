package com.nbcb.factor.web.job.basic.impl;

import cn.hutool.json.JSONUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.JsonUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.entity.BondConfigEntity;
import com.nbcb.factor.enums.BusinessEnum;
import com.nbcb.factor.enums.DataProcessNoticeTypeEnum;
import com.nbcb.factor.enums.SystemEnum;
import com.nbcb.factor.event.StrategyNoticeOutput;
import com.nbcb.factor.output.bondprice.BondPriceOutputResult;
import com.nbcb.factor.web.controller.BondConfigController;
import com.nbcb.factor.web.entity.CurrencyValuation;
import com.nbcb.factor.web.entity.SysDictData;
import com.nbcb.factor.web.job.basic.FactorBasicJob;
import com.nbcb.factor.web.kafka.service.KafkaMessageSendService;
import com.nbcb.factor.web.service.BondConfigService;
import com.nbcb.factor.web.service.CurrencyValuationService;
import com.nbcb.factor.web.service.SysDictDataService;
import com.nbcb.factor.web.util.DateUtils;
import com.nbcb.factor.web.util.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 基础数据处理job
 */
@Component
public class FactorBasicDataProcessJob implements FactorBasicJob {
    @Autowired
    private BondConfigService bondConfigService;
    @Autowired
    private CurrencyValuationService currencyValuationService;
    @Autowired
    private SysDictDataService sysDictDataService;

    /**
     * kafka推送消息service
     */
    private KafkaMessageSendService kafkaMessageSendService;

    private final RedisUtil redisUtil = RedisUtil.getInstance();

    /**
     * 债券类型
     */
    private final static String BUS_SYMBOL_TYPE = "bus_symbol_type";

    @Override
    public Object executeJob(Object param) {
        //查询所有的债券信息
        List<CurrencyValuation> bondList = currencyValuationService.getNewCurrencyValuationList();
        if (CollectionUtils.isEmpty(bondList)) {
            XxlJobLogger.log("查询所有的债券信息为空，不进行入库操作！");
            return ReturnT.FAIL;
        } else {
            XxlJobLogger.log("查询所有的债券信息成功，数量：{}", bondList.size());
        }
        //查询所有债券类型字典数据
        List<SysDictData> sysDictDataList = sysDictDataService.selectSysDictDataByType(BUS_SYMBOL_TYPE);
        if (CollectionUtils.isEmpty(sysDictDataList)) {
            XxlJobLogger.log("查询债券类型为空，不进行入库操作！");
            return ReturnT.FAIL;
        }
        //根据债券类型进行分组
        TreeMap<String, List<SysDictData>> sysDictDataMap = sysDictDataList.stream()
                .collect(Collectors.groupingBy(SysDictData::getDictValue, TreeMap::new, Collectors.toList()));
        //根据债券类型与描述id进行分类
        Map<String, List<CurrencyValuation>> bondGroupList = bondList.stream().collect(Collectors.groupingBy(
                bond -> StringUtils.formatParas("{0}{1}"
                        , bond.getSecuritySuperType(), bond.getSecurityTypeId())));
        //获取当前债券配置信息
        List<BondConfigEntity> entityList = bondConfigService.getBondConfigEntityList();
        XxlJobLogger.log("获取当前债券配置信息数据条数！{}", entityList.size());
        //清空redis配置信息
        if (!CollectionUtils.isEmpty(entityList)) {
            entityList.stream().map(BondConfigEntity::getGroupKey).collect(Collectors.toList()).stream().distinct().forEach(key -> {
                String commonKey = SystemEnum.FACTOR.getKey() + "_" + BusinessEnum.COMMON.getKey() + "_" + key;
                //每个1000个存入redis中，作为现价排序数据模块
                String currencyPriceKey = SystemEnum.FACTOR.getKey() + "_" + BusinessEnum.CURRENT_PRICE.getKey() + "_" + key;
                XxlJobLogger.log("删除redis中的key:[{}]", key);
                redisUtil.delString(commonKey);
                redisUtil.delString(currencyPriceKey);
            });
        }
        XxlJobLogger.log("删除最优行情数据开始！");
        redisUtil.delString(AllRedisConstants.FACTOR_MARKET_PROCESSING_BEST_YIELD);
        XxlJobLogger.log("删除最优行情数据完成！");
        //清空债券信息配置表
        if (bondConfigService.truncateBondConfig()) {
            XxlJobLogger.log("清空债券信息表成功！");
        } else {
            XxlJobLogger.log("清空债券信息表失败，不进行入库操作！");
            return ReturnT.FAIL;
        }
        //组装债券配置信息
        List<BondConfigEntity> bondConfigEntityList = getBondConfigEntities(bondGroupList, sysDictDataMap);
        //入库
        if (this.bondConfigService.insertBondConfig(bondConfigEntityList)) {
            XxlJobLogger.log("入库债券信息配置成功，数量：{}", bondConfigEntityList.size());
        } else {
            XxlJobLogger.log("入库债券信息配置失败！");
            return ReturnT.FAIL;

        }
        //执行完成向kafka推送完成命令
        pushCmdToKafka();
        return ReturnT.SUCCESS;
    }

    /**
     * 推送完成命令到kafka
     */
    private void pushCmdToKafka() {
        //初始化命令对象
        StrategyNoticeOutput output = new StrategyNoticeOutput(new ArrayList<>());
        output.setNoticeType(DataProcessNoticeTypeEnum.BASIC_DATA_PROCESS.getKey());
        try {
            XxlJobLogger.log("定时基础数据处理完成消息推送开始！{}", JSONUtil.toJsonStr(output));
            this.kafkaMessageSendService.sendMessage(StrategyNoticeOutput.TOPIC,
                    JSONUtil.toJsonStr(output));
            XxlJobLogger.log("定时基础数据处理完成消息推送完成！");
        } catch (Exception e) {
            XxlJobLogger.log("定时基础数据处理完成消息异常：{}", e.getMessage());
        }
    }

    /**
     * 组装债券配置信息 并缓存到redis中
     */
    private List<BondConfigEntity> getBondConfigEntities(Map<String, List<CurrencyValuation>> bondGroupList,
                                                         Map<String, List<SysDictData>> sysDictDataMap) {
        List<BondConfigEntity> bondConfigEntityList = new ArrayList<>();
        AtomicInteger id = new AtomicInteger(1);
        //groupKey为债券类型与描述id ps:InterestRate100001
        bondGroupList.forEach((groupKey, groupBondList) -> {
            //遍历分组的债券信息
            int code = 1;
            int j;
            //通用每一组存入的redis的map
            Map<String, String> putCommonMap = new HashMap<>();
            //现价排序每一组存入的redis的map
            Map<String, String> putCurrencyPriceMap = new HashMap<>();
            //遍历组装数据
            for (int i = 1; i <= groupBondList.size(); i++) {
                CurrencyValuation bond = groupBondList.get(i - 1);
                j = (i % 1000) << 1;
                //债券描述映射
                String securityDecMapper;
                if (null != sysDictDataMap && sysDictDataMap.containsKey(bond.getSecurityTypeId())
                        && !CollectionUtils.isEmpty(sysDictDataMap.get(bond.getSecurityTypeId()))) {
                    securityDecMapper = sysDictDataMap.get(bond.getSecurityTypeId()).get(0).getDictLabel();
                } else {
                    XxlJobLogger.log("未找到对应债券类型对应关系，不进行存入redis:{}", bond.getSecurityTypeId());
                    continue;
                }
                BondConfigEntity bondConfigEntity = new BondConfigEntity();
                bondConfigEntity.setId(BigDecimal.valueOf(id.intValue()));//主键递增
                id.incrementAndGet();//主键加1
                bondConfigEntity.setSecurityId(bond.getSecurityTypeId());//债券代码
                bondConfigEntity.setSymbol(bond.getSymbol());//债券简称
                bondConfigEntity.setIssueSize(bond.getIssueSize());//补偿期
                bondConfigEntity.setSecurityType(bond.getSecuritySuperType());//债券类型
                bondConfigEntity.setSecurityDec(bond.getSecurityDec());//债券描述
                bondConfigEntity.setSecurityTypeId(bond.getSecurityTypeId());//债券描述id
                bondConfigEntity.setSecurityDecMapper(securityDecMapper);//债券描述映射
                bondConfigEntity.setValuation(null != bond.getRateValue() ? bond.getRateValue().toString() : "");//中债估值
                bondConfigEntity.setValuationDate(bond.getTradeDt());//中债估值日期
                bondConfigEntity.setTermToMaturityString(remainingTermConversion(bond.getTermToMaturityString()));//剩余期限
                bondConfigEntity.setSecurityKey(String.valueOf(code));//编号
                bondConfigEntity.setCreateTime(DateUtils.getHtime());//创建时间

                //把通用债券配置信息存入map中
                putCommonMap.put(bond.getSecurityId(), JsonUtil.safeToJson(bondConfigEntity));

                //组装现价排序table
                BondPriceOutputResult bondPriceOutputResult = new BondPriceOutputResult();
                bondPriceOutputResult.setValuation(bond.getRateValue());//中债估值
                bondPriceOutputResult.setSecurityId(bond.getSecurityTypeId());//债券代码
                bondPriceOutputResult.setSymbol(bond.getSymbol());//债券简称
                bondPriceOutputResult.setTermToMaturitystring(remainingTermConversion(bond.getTermToMaturityString()));//剩余期限
                bondPriceOutputResult.setSecurityDec(bond.getSecurityDec());//债券描述
                putCurrencyPriceMap.put(bond.getSecurityId(), JsonUtil.safeToJson(bondPriceOutputResult));

                //每1000条设置一个债券分组key
                if (j == 0 || i == groupBondList.size()) {
                    //每1000个存入redis中，作为通用数据模块
                    String commonKey = SystemEnum.FACTOR.getKey() + "_" + BusinessEnum.COMMON.getKey()
                            + "_" + securityDecMapper + "_" + code;
                    redisUtil.setCacheMap(commonKey, putCommonMap);
                    //每1000个存入redis中，作为现价排序数据模块
                    String currencyPriceKey = SystemEnum.FACTOR.getKey() + "_" + BusinessEnum.CURRENT_PRICE.getKey()
                            + "_" + securityDecMapper + "_" + code;
                    redisUtil.setCacheMap(currencyPriceKey, putCurrencyPriceMap);
                    //一批之后重置
                    putCommonMap = new HashMap<>();
                    putCurrencyPriceMap = new HashMap<>();
                    code++;
                }
                bondConfigEntityList.add(bondConfigEntity);
            }
        });
        return bondConfigEntityList;
    }

    /**
     * 剩余期限转化
     */
    public String remainingTermConversion(String termToMaturityString) {
        if (StringUtils.isNotEmpty(termToMaturityString)) {
            String dReplace = termToMaturityString.replace("D", "/365.0");
            String mReplace = termToMaturityString.replace("M", "/12.0");
            String yReplace = termToMaturityString.replace("Y", "/*1.0");
            Double execute = (Double) AviatorEvaluator.execute(yReplace);
            return BigDecimal.valueOf(execute).setScale(4, BigDecimal.ROUND_HALF_UP) + "Y";
        } else {
            //债券已过期
            return "0Y";
        }
    }
}
