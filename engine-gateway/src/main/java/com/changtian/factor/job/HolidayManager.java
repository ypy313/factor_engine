package com.changtian.factor.job;

import com.changtian.factor.common.AllRedisConstants;
import com.changtian.factor.common.JsonUtil;
import com.changtian.factor.common.RedisUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 日期列表管理
 */
@Slf4j
public class HolidayManager {
    private static volatile HolidayManager INSTANCE;
    public static HolidayManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HolidayManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HolidayManager();
                }
            }
        }
        return INSTANCE;
    }

    @Setter
    private static List<String> holidayList = new ArrayList<String>();

    public static List<String> getHolidayList(){
        if (CollectionUtils.isEmpty(holidayList)) {
            //获取本周一日期
            RedisUtil redisUtil = RedisUtil.getInstance();
            String holidayStr = redisUtil.getRiding(AllRedisConstants.FACTOR_PM_SYMBOL);
            if (StringUtils.isNotEmpty(holidayStr)) {
                try {
                    setHolidayList(JsonUtil.toList(holidayStr,String.class));
                }catch (Exception e){
                    log.error("getSettlementDate to list error",e);
                }
            }
        }
        return holidayList;
    }

    /**
     * 更新日期列表数据
     */
    public void setHolidayList(){
        //获取本周一日期
        RedisUtil redisUtil = RedisUtil.getInstance();
        String holidayStr = redisUtil.getRiding(AllRedisConstants.FACTOR_PM_SYMBOL);
        if (StringUtils.isNotEmpty(holidayStr)) {
            try {
                setHolidayList(JsonUtil.toList(holidayStr,String.class));
            } catch (Exception e) {
                log.error("getSettlementDate to list error",e);
            }
        }
    }
}
