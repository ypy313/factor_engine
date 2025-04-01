package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.PmTpHoliday;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 贵金属行情节假日日期
 */
@Repository
public interface PmTpHolidayMapper {
    /**
     * 查询最新参看利率_期限对应的起息日
     */
    List<PmTpHoliday> selectAllPmHoliday();
}
