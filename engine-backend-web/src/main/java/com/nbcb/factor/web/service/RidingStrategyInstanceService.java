package com.nbcb.factor.web.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.json.JSONUtil;
import com.nbcb.factor.common.Constants;
import com.nbcb.factor.web.mapper.CbondanalysiscnbdMapper;
import com.nbcb.factor.web.mapper.StrategyInstanceMapper;
import com.nbcb.factor.web.util.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 加载外汇ta指标计算以你实例service
 */
@Service
public class RidingStrategyInstanceService {
    @Resource
    private StrategyInstanceMapper strategyInstanceMapper;
    @Resource
    private CbondanalysiscnbdMapper cbondanalysiscnbdMapper;

    /**
     * 获取最新收益率曲线
     */
    public String getRidingBondCurveCNBD() {
        return JSONUtil.toJsonStr(this.strategyInstanceMapper.getRidingBondCurveCNBD());
    }
    /**
     * 根据债券代码获取现金流信息
     */
    public String getBondCFByBonds(String bonds){
        return JSONUtil.toJsonStr(this.strategyInstanceMapper.getBondCFByBonds(bonds));
    }
    /**
     * 获取债券实时利率
     */
    public String getBondRealTimeRate(String bonds){
        return JSONUtil.toJsonStr(this.cbondanalysiscnbdMapper.getBondRealTimeRate(bonds));
    }
    /**
     * 回购（repo）利率表
     */
    public String getRepoByInterRate(String showHoldPeiod,String customValue,String interRateType){
        String rate = "";
        if (Constants.RIDING_INTEREST_RATE_TYPE_LATEST_VALUE.equals(interRateType)) {
            //最新中
            rate = this.strategyInstanceMapper.selectLatestValueReporInfo(Constants.FR007_IR);
        }else if(Constants.RIDING_INTEREST_RATE_TYPE_PAST_NEW_VALUE.equals(interRateType)){
            String tradeDt = "";
            //过去均值
            if (Constants.RIDING_SHOW_HOLD_PERIOD_1M.equals(showHoldPeiod)) {
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(1));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_3M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(3));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_6M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(6));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_9M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(9));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_1Y.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(12));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_CUSTOM.equals(showHoldPeiod)){
                long day = DateUtils.timeDiffer(DateUtils.getDateTimeNow(),
                        DateUtils.parseDateToStr(DateUtils.DAYSTR,
                                DateUtils.stringToDate(customValue,DateUtils.DAYSTR)),
                        DateUtils.DAYSTR, DateUnit.DAY);
                Date nowBefore = DateUtils.calcMothsBefore((int)day);
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,nowBefore);
            }
            rate = this.strategyInstanceMapper.selectAverageValueReporRate(Constants.FR007_IR,tradeDt);
        }
        return rate;
    }
    /**
     * shibor利率
     */
    public String getShiborpricesByInterRate(String showHoldPeiod,String customValue,String interRateType){
        String rate = "";
        if (Constants.RIDING_INTEREST_RATE_TYPE_LATEST_VALUE.equals(interRateType)) {
            //最新中
            rate = this.strategyInstanceMapper.selectLatestValueShiborPrices(Constants.SHIBOR1W_IR);
        }else if(Constants.RIDING_INTEREST_RATE_TYPE_PAST_NEW_VALUE.equals(interRateType)) {
            String tradeDt = "";
            //过去均值
            if (Constants.RIDING_SHOW_HOLD_PERIOD_1M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(1));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_3M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(3));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_6M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(6));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_9M.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(9));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_1Y.equals(showHoldPeiod)){
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,DateUtils.calcMothsBefore(12));
            }else if(Constants.RIDING_SHOW_HOLD_PERIOD_CUSTOM.equals(showHoldPeiod)){
                long day = DateUtils.timeDiffer(DateUtils.getDateTimeNow(),
                        DateUtils.parseDateToStr(DateUtils.DAYSTR,
                                DateUtils.stringToDate(customValue,DateUtils.DAYSTR)),
                        DateUtils.DAYSTR, DateUnit.DAY);
                Date nowBefore = DateUtils.calcMothsBefore((int)day);
                tradeDt = DateUtils.parseDateToStr(DateUtils.DAYSTR,nowBefore);
            }
            rate = this.strategyInstanceMapper.selectAverageValueReporRate(Constants.SHIBOR1W_IR,tradeDt);
        }
        return rate;
    }
}
