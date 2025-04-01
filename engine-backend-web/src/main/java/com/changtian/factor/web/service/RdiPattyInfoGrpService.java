package com.changtian.factor.web.service;

import com.changtian.factor.web.config.datasource.DataSource;
import com.changtian.factor.web.config.datasource.DataSourceType;
import com.changtian.factor.web.entity.rdi.RdiPattyInfoGrpEntity;
import com.changtian.factor.web.mapper.RdiPattyInfoGrpMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构信息
 */
@Slf4j
@Service
public class RdiPattyInfoGrpService {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 查询所有的海泉机构信息
     */
    @DataSource(value = DataSourceType.smds)
    public List<RdiPattyInfoGrpEntity> selectRdiPattyInfoGrpEntityList() {
        List<RdiPattyInfoGrpEntity> rdiPattyInfoGrpEntityList = new ArrayList<>();
        Cursor<RdiPattyInfoGrpEntity> cursor = null;
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
            RdiPattyInfoGrpMapper rdi = sqlSession.getMapper(RdiPattyInfoGrpMapper.class);
            cursor = rdi.selectRdiPattyInfoGrpEntityList();
            cursor.forEach(rdiPattyInfoGrpEntityList::add);
        } catch (Exception e) {
            log.error("通过游标查询RDI所有的机构信息失败！{}", e.getMessage());
        } finally {
            if (null != cursor) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    log.error("通过游标查询RDI所有的机构信息完成后，关闭游标失败！{}", e.getMessage());
                }
            }
            if(null != sqlSession) {
                try{
                    sqlSession.close();
                }catch (Exception e){
                    log.error("通过游标查询RDI所有的机构信息完成后，关闭sqlSession失败！{}",e.getMessage());
                }
            }
        }
        return rdiPattyInfoGrpEntityList;
    }
}
