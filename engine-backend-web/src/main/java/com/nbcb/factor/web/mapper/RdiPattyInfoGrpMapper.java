package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import com.nbcb.factor.web.entity.rdi.RdiRattyInfoGrpEntity;
import org.apache.ibatis.cursor.Cursor;

/**
 * 机构信息mapper
 */
public interface RdiPattyInfoGrpMapper {
    /**
     * 查询所有的机构信息
     */
    @DataSource(value = DataSourceType.smds)
    Cursor<RdiRattyInfoGrpEntity> selectRdiPattyInfoGrpEntityList();
}
