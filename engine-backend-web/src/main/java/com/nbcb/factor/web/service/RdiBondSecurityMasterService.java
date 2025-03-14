package com.nbcb.factor.web.service;

import com.nbcb.factor.web.config.datasource.DataSource;
import com.nbcb.factor.web.config.datasource.DataSourceType;
import com.nbcb.factor.web.entity.rdi.RdiBondSecurityMasterEntity;
import com.nbcb.factor.web.mapper.RdiBondSecurityMasterMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 债券信息数据Service
 */
@Slf4j
@Service
public class RdiBondSecurityMasterService {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 查询所有的海泉机构信息
     */
    @DataSource(value = DataSourceType.smds)
    public List<RdiBondSecurityMasterEntity> selectRdiBondSecurityMasterEntityList() {
        List<RdiBondSecurityMasterEntity> bondSecurityMasterEntityList = new ArrayList<>();
        Cursor<RdiBondSecurityMasterEntity> cursor = null;
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
            RdiBondSecurityMasterMapper rdi = sqlSession.getMapper(RdiBondSecurityMasterMapper.class);
            cursor = rdi.selectRdiBondSecurityMasterEntityList();
            cursor.forEach(bondSecurityMasterEntityList::add);
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
        return bondSecurityMasterEntityList;
    }
}
