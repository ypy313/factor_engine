package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.rdi.RdiPattyInfoGrpEntity;
import org.apache.ibatis.cursor.Cursor;

/**
 * 机构信息mapper
 */
public interface RdiPattyInfoGrpMapper {
    /**
     * 查询所有的机构信息
     */
    @DataSource(value = DataSourceType.smds)
    Cursor<RdiPattyInfoGrpEntity> selectRdiPattyInfoGrpEntityList();
}
