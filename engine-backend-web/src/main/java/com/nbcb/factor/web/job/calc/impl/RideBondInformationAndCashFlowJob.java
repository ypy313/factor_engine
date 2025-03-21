package com.nbcb.factor.web.job.calc.impl;

import cn.hutool.json.JSONUtil;
import com.nbcb.factor.common.AllRedisConstants;
import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.common.RedisUtil;
import com.nbcb.factor.entity.BondCFVo;
import com.nbcb.factor.entity.CbondCurveCnbdVo;
import com.nbcb.factor.entity.riding.Cbondanalysiscnbd;
import com.nbcb.factor.web.job.calc.FactorCalcJob;
import com.nbcb.factor.web.mapper.CbondanalysiscnbdMapper;
import com.nbcb.factor.web.mapper.RidingYieldCurveMapper;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import com.nbcb.factor.web.service.HolidayService;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 骑乘策略-同步债券信息/现金流表数据
 *
 * @author k11866
 * @date 2023/5/8 14:58
 */
@Component
public class RideBondInformationAndCashFlowJob implements FactorCalcJob {
    @Autowired
    private RidingYieldCurveMapper ridingYieldCurveMapper;
    @Autowired
    private StrategyInstanceMapper strategyInstanceMapper;
    @Autowired
    private CbondanalysiscnbdMapper cbondanalysiscnbdMapper;
    @Autowired
    private HolidayService holidayService;
    private final RedisUtil redisUtil = RedisUtil.getInstance();

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("RideBondInformationAndCashFlowJob cnbdJob");
            XxlJobLogger.log("the param is {}", param);

            // 默认为今日
            String daystr = DateUtil.getDaystr();
            // 参数为空，执行默认日期，参数不为空，执行后续逻辑
            if (!StringUtils.equals(param, "")) {
                String[] dateArr = param.split(",");
                if (dateArr.length == 1) { // 参数唯一
                    if (DateUtil.isValidDate(dateArr[0])) {
                        daystr = dateArr[0]; // 执行指定日期
                    } else {
                        XxlJobLogger.log("Backup failure. Backup time:{},Parameter error{}", DateUtil.getSendingTimeStr(), param);
                        return ReturnT.FAIL; // 不是合法日期，不执行
                    }
                } else if (dateArr.length > 1) { // 参数大于1，不执行
                    XxlJobLogger.log("Backup failure. Execution time:{},Perform parameter{}", DateUtil.getSendingTimeStr(), param);
                    return ReturnT.FAIL;
                }
            }

            // 债券信息表数据处理
            // 获取今日日期，格式为yyyyMMdd
            if (ridingYieldCurveMapper.selectCbonddescriptionInfa(daystr) <= 0) {
                // 删除前的查询全量更新的数据是否有值，如果没有，打印日志
                XxlJobLogger.log("没有债券信息表同步的数据，债券信息表数据不同步！");
            } else {
                XxlJobLogger.log("债券信息表同步开始。。。。");
                // 删除原债券信息表
                ridingYieldCurveMapper.truncateCbonddescription();
                // 通过factor.Cbonddescription_info过滤创建新的债券信息表数据
                ridingYieldCurveMapper.insertCbonddescription(daystr);
                XxlJobLogger.log("债券信息表同步结束！");
            }

            // 现金流数据处理(全量抽取)
            // 获取昨日日期，格式为yyyyMMdd
            XxlJobLogger.log("现金流数据同步开始。。。");
            // 查询是否有现金流数据备份
            if (ridingYieldCurveMapper.selectCBONDCF() <= 0) {
                // 删除前的查询全量更新的数据是否有值，如果没有，打印日志
                XxlJobLogger.log("没有现金流表同步的数据，债券信息表数据不同步！");
            } else {
                ridingYieldCurveMapper.truncateCbondCf();
                // 通过factor.CBONDCF_info过滤创建新的债券信息表数据
                ridingYieldCurveMapper.insertAllCbondCfByCbonddescription();
            }
            Date date = DateUtil.dateToDateFormat(DateUtil.calcDaysBefore(1), DateUtil.DAYSTR);

            // 收益率数据处理
            XxlJobLogger.log("收益率数据同步开始。。。");
            ridingYieldCurveMapper.insertCbondCurveCNBD(DateUtil.parseDateToStr(DateUtil.DAYSTR, date)));
            XxlJobLogger.log("收益率数据同步结束！");

        // 缓存现金流数据到redis
        XxlJobLogger.log("缓存现金流数据到redis开始。。。");
        List<BondCFVo> bondCFVoList = strategyInstanceMapper.getBondCFAll();
        if (!CollectionUtils.isEmpty(bondCFVoList)) {
            // 根据债券代码分组
            Map<String, List<BondCFVo>> groupList = bondCFVoList.stream()
                    .collect(Collectors.groupingBy(BondCFVo::getInfoWindCode));

            // 缓存到redis中
            groupList.forEach((key, value) -> redisUtil.setRiding(
                    AllRedisConstants.FACTOR_COMMON_CBONDCF,
                    JSONUtil.toJsonStr(value)));
        }
        XxlJobLogger.log( "缓存现金流数据到redis结束！");

        // 缓存中债估值
        XxlJobLogger.log( "缓存中债估值数据到redis开始。。。");
        List<Cbondanalysiscnbd> cbondAnalysisCnbdList =
                this.cbondanalysiscnbdMapper.selectBondRealTimeRateAll();
        if (!CollectionUtils.isEmpty(cbondAnalysisCnbdList)) {
            cbondAnalysisCnbdList.forEach(c -> redisUtil.setRiding(
                    AllRedisConstants.FACTOR_COMMON_CBONDANALYSISCNBD,
                    JSONUtil.toJsonStr(c)));
        }
        XxlJobLogger.log( "缓存中债估值数据到redis结束！");

        // 缓存收益率曲线
        XxlJobLogger.log("缓存收益率曲线数据到redis开始。。。");
        List<CbondCurveCnbdVo> cbondCurveCnbdVoList = strategyInstanceMapper.getRidingBondCurveCNBD();
        if (!CollectionUtils.isEmpty(cbondCurveCnbdVoList)) {
            redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_CBOND_CURVE_CNBD,
                    JSONUtil.toJsonStr(cbondCurveCnbdVoList));
        }
        XxlJobLogger.log("缓存收益率曲线数据到redis结束！");
        // 假日数据缓存到redis
        XxlJobLogger.log("假日数据缓存到redis开始。。。");
        String stringList = this.holidayService.selectRdiHoliday();
        if (StringUtils.isNotEmpty(stringList)) {
            redisUtil.setRiding(AllRedisConstants.FACTOR_COMMON_HOLIDAY,
                    stringList);
        }
        XxlJobLogger.log( "假日数据缓存到redis结束！");
        return ReturnT.SUCCESS;
    } catch (Exception e) {
        XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
        return ReturnT.FAIL;
    }
}
}