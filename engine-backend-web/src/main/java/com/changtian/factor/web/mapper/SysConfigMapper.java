package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.dto.SysConfig;
import org.springframework.stereotype.Repository;

/**
 * 参数设置
 */
@Repository
public interface SysConfigMapper {
    /**
     * 查询参数配置信息
     */
    SysConfig selectConfig(SysConfig config);
}
