package com.changtian.factor.web.mapper;

import com.changtian.factor.entity.BondConfigEntity;
import org.apache.ibatis.cursor.Cursor;

/**
 * 债券配置mapper
 */
public interface BondConfigMapper {
    /**
     * 清空表
     */
    void truncateBondConfig();
    /**
     * 插入债券配置信息
     */
    int insertBondConfig(BondConfigEntity bondConfigEntity);
    /**
     * 获取所有的配置信息
     */
    Cursor<BondConfigEntity> selectBondConfigEntityList();
}
