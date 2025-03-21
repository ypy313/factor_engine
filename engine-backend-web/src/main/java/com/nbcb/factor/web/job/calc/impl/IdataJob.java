package com.nbcb.factor.web.job.calc.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.nbcb.factor.web.entity.dto.TimeConfigDto;
import com.nbcb.factor.web.job.calc.FactorCalcJob;
import com.nbcb.factor.web.mapper.IdataMapper;
import com.nbcb.factor.web.util.DateUtils;
import com.nbcb.factor.web.util.ExceptionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 将info数据同步到业务表
 * REPORT_DM_PERFORMANCE_INFO 数据同步到
 * IndvBondCounterpartyAnalysis 22
 * IndvBondLoanAnalysis 23
 */
@Component
public class IdataJob implements FactorCalcJob {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private IdataMapper idataMapper;

    private static final String DAY_STRING = "yyyyMMdd";

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("IdataJob is start");
            TimeConfigDto timeConfigDto = new TimeConfigDto();
            if (!"".equals(param)) {
                timeConfigDto = JSONUtil.toBean(param, TimeConfigDto.class);
            }
            // 判断如果param没有值 或者结束时间早于开始时间 或者开始时间或结束时间有一个没有值，则默认清理三天数据
            // 否则根据传参数据修改数据，如果超过15天 默认修改为10天
            if (ObjectUtil.isEmpty(timeConfigDto) || StringUtils.isEmpty(timeConfigDto.getBeginTime()) ||
                    StringUtils.isEmpty(timeConfigDto.getEndTime()) ||
                    (StringUtils.isEmpty(timeConfigDto.getBeginTime()) && StringUtils.isEmpty(timeConfigDto.getEndTime())) ||
                    !isBeginBeforeEndTime(timeConfigDto)) {
                LocalDate today = LocalDate.now();
                LocalDate localDate = today.minusDays(3);
                String date = localDate.format(DateTimeFormatter.ofPattern(DAY_STRING));
                timeConfigDto.setBeginTime(date);
                String dateTime = DateUtils.dateTime();
                timeConfigDto.setEndTime(dateTime);
            } else {
                // 根据传递参数，将其转化为可执行的日期
                convertTime(timeConfigDto);
            }
            XxlJobLogger.log("同步清理的日期为 beginTime is {} " +
                    "endTime is {}", timeConfigDto.getBeginTime(), timeConfigDto.getEndTime());

            // 查询是否有idata个债信息
            if (idataMapper.selectIdataDebtMarketCount() <= 0) {
                // 删除前的查询个债更新的数据是否存在，如果没有，打印日志
                XxlJobLogger.log("没有idata个债信息同步的数据，idata个债信息表数据不同步！");
            } else {
                LOGGER.info("idata is start IndvBondTradingAction 21");
                // 默认清理 前三天的数据
                cleanIndvbondTradingAction(timeConfigDto);
                // 使用merge语法，将数据差异同步到 INDVBOND_TRADING_ACTION
                idataMapper.mergeIndvBondTradingAction();
                LOGGER.info("idata is end IndvBondTradingAction 21");

                LOGGER.info("idata is start IndvBondLoanAnalysis 23");
                // 默认清理 前三天的数据
                cleanIndvBondLoanAnalysis(timeConfigDto);
                // 使用merge语法，将数据差异同步到 INDVBOND_LOAN_ANALYSIS
                idataMapper.mergeIndvBondLoanAnalysis();
                LOGGER.info("idata is end IndvBondLoanAnalysis 23");

                LOGGER.info("idata is start BondLendingSummaryAnalysis 24");
                // 默认清理 前三天的数据
                cleanBondLendingSummaryAnalysis(timeConfigDto);
                // 使用merge语法，将数据差异同步到 BOND_LENDING_SUMMARY_ANALYSIS
                idataMapper.mergeBondLendingSummaryAnalysis();
                LOGGER.info("idata is end BondLendingSummaryAnalysis 24");
            }
            if (idataMapper.selectIdataOrgCount() <= 0) {
                // 删除前的查询个债更新的数据是否存在，如果没有，打印日志
                XxlJobLogger.log("没有idata机构同步的数据，idata机构数据不同步！");
            } else {
                LOGGER.info("idata is start report_org_behavior");
                // 默认清理 前三天的数据
                cleanReportOrgBehavior(timeConfigDto);
                // 使用merge语法，将数据差异同步到 report_org_behavior
                idataMapper.mergeReportOrgBehavior();
                LOGGER.info("idata is end report_org_behavior");
            }
            XxlJobLogger.log("IdataJob is end");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("跑批失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }


        /**
         * 清理 InvBondCounterpartyAnalysis 表空间
         */
        public void cleanIndvBondCounterpartyAnalysis(TimeConfigDto timeConfigDto) {

            idataMapper.deleteIndvBondCounterpartyAnalysis(timeConfigDto);
            idataMapper.createTemplIndvBondCounterpartyAnalysis();
            idataMapper.insertTempIndvBondCounterpartyAnalysis();
            idataMapper.truncateIndvBondCounterpartyAnalysis();
            idataMapper.insertIndvBondCounterpartyAnalysis();
            idataMapper.dropTempIndvBondCounterpartyAnalysis();
        }

        /**
         * 清理 InvBondTradingAction 表空间
         */
        public void cleanIndvbondTradingAction(TimeConfigDto timeConfigDto) {
            idataMapper.deleteIndvbondTradingAction(timeConfigDto);
            idataMapper.createTempIndvbondTradingAction();
            idataMapper.insertTempIndvbondTradingAction();
            idataMapper.truncateIndvbondTradingAction();
            idataMapper.insertIndvbondTradingAction();
        }

        /**
         * 清理 InvBondLoanAnalysis 表空间
         */
        public void cleanIndvBondLoanAnalysis(TimeConfigDto timeConfigDto) {
            idataMapper.deleteIndvBondLoanAnalysis(timeConfigDto);
            idataMapper.createTempIndvBondLoanAnalysis();
            idataMapper.insertTempIndvBondLoanAnalysis();
            idataMapper.truncateIndvBondLoanAnalysis();
            idataMapper.insertIndvBondLoanAnalysis();
            idataMapper.dropTempIndvBondLoanAnalysis();
        }

        /**
         * 清理 BondLendingSummaryAnalysis 表空间
         */
        public void cleanBondLendingSummaryAnalysis(TimeConfigDto timeConfigDto) {
            idataMapper.deleteBondLendingSummaryAnalysis(timeConfigDto);
            idataMapper.createTempBondLendingSummaryAnalysis();
            idataMapper.insertTempBondLendingSummaryAnalysis();
            idataMapper.truncateBondLendingSummaryAnalysis();
            idataMapper.insertBondLendingSummaryAnalysis();
            idataMapper.dropTempBondLendingSummaryAnalysis();
        }

        /**
         * 清理 ReportOrgBehavior 表空间
         */
        public void cleanReportOrgBehavior(TimeConfigDto timeConfigDto) {
            idataMapper.deleteReportOrgBehavior(timeConfigDto);
            idataMapper.createTempReportOrgBehavior();
            idataMapper.insertTempReportOrgBehavior();
            idataMapper.truncateReportOrgBehavior();
            idataMapper.insertReportOrgBehavior();
            idataMapper.dropTempReportOrgBehavior();
        }

        /**
         * 根据传递参数 判断起始日期是否为15天前的日期，如果是15天前的日期，则清理10天前到今日的数据
         * 否则根据参数清理
         */
        public void convertTime(TimeConfigDto timeConfigDto) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAY_STRING);
            // 解析字符串日期
            LocalDate date = LocalDate.parse(timeConfigDto.getBeginTime(), formatter);
            // 获取今日日期
            LocalDate today = LocalDate.now();
            // 计算15天前的日期
            LocalDate fifteenDaysAgo = today.minusDays(15);
            // 比较日期
            boolean isFifteenDaysAgo = date.isBefore(fifteenDaysAgo);
            if (isFifteenDaysAgo) {
                timeConfigDto.setBeginTime(today.minusDays(10).format(formatter));
                timeConfigDto.setBeginTime(today.format(formatter.ofPattern(DAY_STRING)));
            }
        }

        /**
         * 判断开始时间小于结束时间
         */
        public boolean isBeginBeforeEndTime(TimeConfigDto timeConfigDto) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAY_STRING);
            LocalDate beginTime = LocalDate.parse(timeConfigDto.getBeginTime(), formatter);
            LocalDate endTime = LocalDate.parse(timeConfigDto.getEndTime(), formatter);
            return beginTime.isBefore(endTime);
        }
    }