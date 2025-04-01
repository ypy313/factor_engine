package com.changtian.factor.web.job.calc.impl;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.web.job.calc.FactorCalcJob;
import com.changtian.factor.web.mapper.CbondanalysiscnbdMapper;
import com.changtian.factor.web.util.ExceptionUtils;
import com.changtian.factor.web.util.StringUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时复制数据
 */
@Component
public class CbondanalysiscnbdJob implements FactorCalcJob {

    @Autowired
    private CbondanalysiscnbdMapper cbondanalysiscnbdMapper;

    @Override
    public ReturnT executeJob(String param) {
        try {
            XxlJobLogger.log("CbondaanalysiscnbdJob cnbdJob");
            XxlJobLogger.log("The param is {}", param);

            // 默认为昨日，定时任务执行时间是当天凌晨，复制的数据是昨日产生
            String dateTime = DateUtil.format(DateUtil.calcDaysBefore(1), DateUtil.DAYSTR);
            // 参数为空，执行默认日期，参数不为空，执行传参日期
            if (!StringUtils.equals(param, "")) {
                String[] dateArr = param.split(",");
                if (dateArr.length == 1) {// 参数唯一
                    // 判断参数的格式是否为"yyyyMMdd"格式的合法日期字符串
                    if (DateUtil.isValidDate(dateArr[0])) {
                        dateTime = dateArr[0];// 执行指定日期
                    } else {
                        XxlJobLogger.log("Backup failure. Backup time:{},Parameter error{}", DateUtil
                                .getSendingTimeStr(), param);
                        return ReturnT.FAIL;// 不是合法日期，不执行
                    }
                } else if (dateArr.length > 1) {// 参数大于1，不执行
                    XxlJobLogger.log("Backup failure. Execution time:{},Perform parameter{}", DateUtil
                            .getSendingTimeStr(), param);
                    return ReturnT.FAIL;
                }
            }

            // 获取需备份的条数
            int sumCount = cbondanalysiscnbdMapper.getCbondanalysiscnbdCount(dateTime);
            if (sumCount > 0) {
                cbondanalysiscnbdMapper.deleteCbondanalysiscnbd(dateTime);// 执行删除昨日数据
                int cnbdCount = cbondanalysiscnbdMapper.backupCopyCbondanalysiscnbd(dateTime);// 备份，返回本次已备份条数

                if (sumCount == cnbdCount) {// 相等，执行成功
                    XxlJobLogger.log("Backup succeeded. Execution time:{},Backup time:{},The backup Data:{}",
                            DateUtil.getSendingTime(), dateTime, cnbdCount);
                    return ReturnT.SUCCESS;
                } else {// 不相等，删除本次备份数据，前端配置程序再次执行
                    // 执行删除昨日数据
                    XxlJobLogger.log("Backup failure. Execution time:{},Backup time:{},This time should be backup Data{}",
                            DateUtil.getSendingTime(), dateTime, cnbdCount);
                    cbondanalysiscnbdMapper.deleteCbondanalysiscnbd(dateTime);
                    return ReturnT.FAIL;
                }
            } else {// 无备份数据
                XxlJobLogger.log("Backup succeeded. Execution time:{},Backup time:{},The number of backups is zero",
                        DateUtil.getSendingTime(), dateTime);
                return ReturnT.SUCCESS;
            }
        } catch (Exception e) {
            XxlJobLogger.log("批量失败，原因为：{}", ExceptionUtils.toString(e));
            return ReturnT.FAIL;
        }
    }
}
