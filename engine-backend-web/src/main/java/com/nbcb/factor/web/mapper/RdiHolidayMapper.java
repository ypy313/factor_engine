package com.nbcb.factor.web.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 假日数据
 */
public interface RdiHolidayMapper {
    /**
     * 查询所有的假日数据
     */
    List<String> selectRdiHoliday();
    /**
     * 根据日期查询是否是假日
     */
    List<String> selectRdiHolidayByDate(@Param("date") String date);
}
