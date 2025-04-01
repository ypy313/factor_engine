package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.SysDictData;

import java.util.List;

/**
 * 基础数据Mapper
 */
public interface SysDictDataMapper {
    /**
     * 根据字典类型查询字典数据
     */
    List<SysDictData> selectDictDataByType(String dictType);
}
