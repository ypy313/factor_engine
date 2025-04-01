package com.changtian.factor.web.service;

import com.changtian.factor.web.entity.po.RateSwapClosingCurvePO;
import com.changtian.factor.web.mapper.RateSwapClosingCurveMapper;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 行情利率互换曲线收盘数据Service
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RateSwapClosingCurveService {
    @Autowired
    private RateSwapClosingCurveMapper rateSwapClosingCurveMapper;

    /**
     * 批量保存数据，已存在的该日数据先删除
     */
    public void replaceAll(List<RateSwapClosingCurvePO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        int delCount = rateSwapClosingCurveMapper.deleteByTradeDt(list.get(0).getTradeDt());
        XxlJobLogger.log("本次删除：{}条",delCount);
        int insertCount = rateSwapClosingCurveMapper.insertBatch(list);
        XxlJobLogger.log("本次插入：{}条",insertCount);
    }
    /**
     * 查询该日期中数据库中存在的数据量
     */
    public int selectNumByDate(String date){
        return rateSwapClosingCurveMapper.selectNumByTradeDt(date);
    }
}
