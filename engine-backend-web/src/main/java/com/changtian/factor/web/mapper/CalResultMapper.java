package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.CalResultData;

public interface CalResultMapper {
    /**
     * 新增指标计算结果
     */
    @DataSource(value = DataSourceType.smds)
    int insertCalResult(CalResultData calResultData);
}
