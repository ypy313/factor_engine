package com.changtian.factor.web.service;

import com.changtian.factor.web.entity.FactorInstanceConfigData;
import com.changtian.factor.web.entity.FactorInstanceDefinitionSymbol;
import com.changtian.factor.web.mapper.FactorPmSymbolMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class FactorPmSymbolService {
    @Autowired
    private FactorPmSymbolMapper factorPmSymbolMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    /**
     * 查询历史ohlc数据
     */
    public List<String > getAllSymbolList(Long definitionId){
        return  factorPmSymbolMapper.getAllSymbolList(definitionId);
    }
    /**
     * 查询因子管理配置表中存在的指标与货币对
     */
    public List<FactorInstanceDefinitionSymbol> getAllIndexCategorySymbol(Long definitionId){
        return factorPmSymbolMapper.getAllIndexCategorySymbol(definitionId);
    }

    public void  insertSymbolList(List<FactorInstanceConfigData> addSymbolList){
        if (CollectionUtils.isEmpty(addSymbolList)) {
            log.error("需要插入的资产数据为null");
        }
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FactorPmSymbolMapper configMapper = sqlSession.getMapper(FactorPmSymbolMapper.class);
            addSymbolList.forEach(configMapper::insertSymbolList);
            sqlSession.commit();
        }catch (Exception e){
            log.error("插入贵金属资产失败，进行回滚！{}",e.getMessage());
            sqlSession.rollback();
        }finally {
            if (null != null) {
                try{
                    sqlSession.close();
                }catch (Exception e){
                    log.error("插入贵金属资产完成关闭sqlSession失败！{}",e.getMessage());
                }
            }
        }
    }

    public List<FactorInstanceDefinitionSymbol> selectConfigInfoId(Long definitionId){
        return factorPmSymbolMapper.selectConfigInfoId(definitionId);
    }

    public Integer selectMaxDisplayOrder(Long configInfo){
        return factorPmSymbolMapper.selectMaxDisplayOrder(configInfo);
    }
    public Long getMaxConfigDataId(){
        return factorPmSymbolMapper.getMaxConfigDataId();
    }

}
