package com.changtian.factor.web.service;

import cn.hutool.json.JSONUtil;
import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.PmTpHoliday;
import com.changtian.factor.web.mapper.PmTpHolidayMapper;
import com.changtian.factor.web.mapper.RdiHolidayMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 假日数据
 */
@Service
public class HolidayService {
    @Autowired
    private RdiHolidayMapper rdiHolidayMapper;
    @Autowired
    private PmTpHolidayMapper pmTpHolidayMapper;
    /**
     * 查询所有的假日数据
     */
    @DataSource(value = DataSourceType.smds)
    public String selectRdiHoliday(){
        return JSONUtil.toJsonStr(this.rdiHolidayMapper.selectRdiHoliday());
    }
    /**
     * 根据日期查询是否是假日
     */
    @DataSource(value = DataSourceType.smds)
    public String selectRdiHolidayByDate(String date){
        return JSONUtil.toJsonStr(this.rdiHolidayMapper.selectRidHolidayByDate(date));
    }
    /**
     * 根据日期查询是否是假日
     */
    @DataSource(value = DataSourceType.smds)
    public List<PmTpHoliday> selectPmHolidayByDate(){
        return this.pmTpHolidayMapper.selectAllPmHoliday();
    }

}
