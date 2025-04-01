package com.changtian.factor.web.service;

import com.changtian.factor.web.entity.SysDictData;
import com.changtian.factor.web.mapper.SysDictDataMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典数据service
 */
@Service
public class SysDictDataService {
    @Resource
    private SysDictDataMapper sysDictDataMapper;

    /**
     * 根据类型查询所有的字典数据
     */
    public List<SysDictData> selectSysDictDataByType(String type){
        return sysDictDataMapper.selectDictDataByType(type);
    }
}
