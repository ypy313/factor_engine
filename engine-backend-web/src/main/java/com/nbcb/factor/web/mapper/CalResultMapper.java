package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import com.nbcb.factor.web.entity.CalResultData;

public interface CalResultMapper {
    /**
     * 新增指标计算结果
     */
    @DataSource(value = DataSourceType.smds)
    int insertCalResult(CalResultData calResultData);
}
