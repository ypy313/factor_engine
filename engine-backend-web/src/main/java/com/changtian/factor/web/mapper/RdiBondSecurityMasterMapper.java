package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.rdi.RdiBondSecurityMasterEntity;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;

/**
 * 债券信息mapper
 */
@Repository
public interface RdiBondSecurityMasterMapper {

    /**
     * 查询所有的债券信息
     */
    @DataSource(value = DataSourceType.smds)
    Cursor<RdiBondSecurityMasterEntity> selectRdiBondSecurityMasterEntityList();
}
