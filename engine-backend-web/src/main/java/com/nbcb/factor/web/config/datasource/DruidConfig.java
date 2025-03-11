package com.nbcb.factor.web.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.nbcb.factor.web.config.datasource.properties.DruidProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * druid 配置多数据源
 */
@Configuration
public class DruidConfig {
    /**
     * master 数据源注入（此处图片中注释为"factor"，可能是特定项目命名）
     *
     * @param druidProperties druidProperties 配置属性
     * @return 数据源对象
     */
    @Bean(name = "factorDataSource")
    @ConfigurationProperties("spring.datasource.factor")
    @Qualifier("factorDataSource")
    public DataSource factorDataSource(DruidProperties druidProperties) {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    /**
     * stc 数据源注入（此处根据图片中的"smds"进行修正）
     *
     * @param druidProperties druidProperties 配置属性
     * @return 数据源对象
     */
    @Bean(name = "smdsDataSource")
    @ConfigurationProperties("spring.datasource.smds")
    @Qualifier("smdsDataSource")
    public DataSource smdsDataSource(DruidProperties druidProperties) {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    /**
     * 动态数据源配置
     *
     * @param factorDataSource factor 数据源
     * @param smdsDataSource   smds 数据源
     * @return 动态数据源对象
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(@Qualifier("factorDataSource") DataSource factorDataSource,
                                        @Qualifier("smdsDataSource") DataSource smdsDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.factor.name(), factorDataSource);
        targetDataSources.put(DataSourceType.smds.name(), smdsDataSource);
        return new DynamicDataSource(factorDataSource, targetDataSources);
    }

}
