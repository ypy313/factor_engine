package com.changtian.factor.web.service;

import com.changtian.factor.entity.BondConfigEntity;
import com.changtian.factor.web.mapper.BondConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.apache.ibatis.cursor.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * 债券配置信息service
 */
@Slf4j
@Service
public class BondConfigService {
    @Autowired
    private BondConfigMapper bondConfigMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    /**
     * 清空表
     */
    public boolean truncateBondConfig(){
        boolean flag = false;
        try{
            bondConfigMapper.truncateBondConfig();
            flag = true;
        }catch (Exception e){
            log.error("清空债券配置信息表失败！{}",e.getMessage());
        }
        return flag;
    }

    /**
     * 插入债券配置信息
     */
    public boolean insertBondConfig(List<BondConfigEntity> bondConfigEntityList){
        boolean flag = false;
        if (CollectionUtils.isEmpty(bondConfigEntityList)) {
            log.error("插入债券配置信息时，债券信息为空！");
            return false;
        }
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            BondConfigMapper configMapper = sqlSession.getMapper(BondConfigMapper.class);
            bondConfigEntityList.forEach(configMapper::insertBondConfig);
            sqlSession.commit();
            flag = true;
        }catch (Exception e){
            log.error("插入债券配置信息表失败，进行回滚！{}",e.getMessage());
            assert sqlSession != null;
            sqlSession.rollback();
        }finally {
            if (null != sqlSession) {
                try {
                    sqlSession.close();
                }catch (Exception e){
                    log.error("插入债券配置信息完成关闭sqlSession失败！{}",e.getMessage());
                }
            }
        }
        return flag;
    }

    /**
     * 查询所有债券最新的中债估值
     */
    public List<BondConfigEntity> getBondConfigEntityList(){
        List<BondConfigEntity> currencyValuationList = new ArrayList<>();
        Cursor<BondConfigEntity> cursor = null;
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
            BondConfigMapper configMapper = sqlSession.getMapper(BondConfigMapper.class);
            cursor = configMapper.selectBondConfigEntityList();
            cursor.forEach(currencyValuationList::add);
        }catch (Exception e){
            log.error("通过游标查询所有债券最新的中债估值失败！{}",e.getMessage());
        }finally {
            if (null != cursor) {
                try{
                    cursor.close();
                }catch (Exception e){
                    log.error("通过游标查询所有债券最新的中债估值完成后，关闭游标失败！{}",e.getMessage());
                }
            }
            if (null != sqlSession) {
                try{
                    sqlSession.close();
                }catch (Exception e){
                    log.error("通过游标查询所有债券最新的中债估值完成后，关闭sqlSession失败！{}",e.getMessage());
                }
            }
        }
        return currencyValuationList;
    }
}
