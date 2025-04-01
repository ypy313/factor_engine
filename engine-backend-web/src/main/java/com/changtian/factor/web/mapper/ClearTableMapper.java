package com.changtian.factor.web.mapper;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;

public interface ClearTableMapper {
    /**
     * 创建外汇临时表
     */
    @DataSource(value = DataSourceType.smds)
    void createFxTempTable();
    /**
     * 清理外汇当前表
     */
    @DataSource(value = DataSourceType.smds)
    void clearFxTable();
    /**
     * 迁移数据回外汇当前表
     */
    @DataSource(value = DataSourceType.smds)
    void copyToFxTable();
    /**
     * 删除外汇临时表
     */
    @DataSource(value = DataSourceType.smds)
    void dropFxTempTable();
    /**
     * 创建贵金属临时表
     */
    @DataSource(value = DataSourceType.smds)
    void createPmTempTable();
    /**
     * 清理贵金属当前表
     */
    @DataSource(value = DataSourceType.smds)
    void clearPmTable();
    /**
     * 迁移数据回贵金属当前表
     */
    @DataSource(value = DataSourceType.smds)
    void copyToPmTable();
    /**
     * 删除贵金属临时表
     */
    @DataSource(value = DataSourceType.smds)
    void dropPmTempTable();
    /**
     * 创建计算结果临时表
     */
    @DataSource(value = DataSourceType.smds)
    void createCalTempTable();
    /**
     * 清理计算结果当前表
     */
    @DataSource(value = DataSourceType.smds)
    void clearCalTable();
    /**s
     * 迁移数据回计算结果当前表
     */
    @DataSource(value = DataSourceType.smds)
    void copyToCalTable();
    /**
     * 删除计算结果临时表
     */
    @DataSource(value = DataSourceType.smds)
    void dropCalTempTable();
}
