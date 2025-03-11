package com.nbcb.factor.web.config.datasource.properties;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * druid配置属性
 */
@Slf4j
public class DruidProperties {
    public DruidDataSource dataSource(DruidDataSource druidDataSource) {
        return druidDataSource;
    }
}
