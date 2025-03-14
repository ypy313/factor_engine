package com.nbcb.factor.web.service;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.web.entity.CurrencyValuation;
import com.nbcb.factor.web.mapper.CurrencyValuationMapper;
import com.nbcb.factor.web.response.exception.CalcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CurrencyValuationService {
    @Autowired
    private CurrencyValuationMapper currencyValuationMapper;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public Map<String, Object> getCurrencyValuation(String bondCode){
        //开始日期，固定取最新10日数据中的最新一条
        String startDate = DateUtil.format(DateUtil.calcDaysBefore(10), DateUtil.DAYSTR);
        CurrencyValuation currencyValuation = currencyValuationMapper.getCurrencyValuation(startDate, bondCode);
        Map<String,Object> map = null;
        if (currencyValuation != null) {
            map = new HashMap<>();
            map.put("bond_code", currencyValuation.getSecurityId());
            map.put("cnbd_value",currencyValuation.getRateValue());
            map.put("value_date",currencyValuation.getTradeDt());

        }else {
            log.error("currencyValuetion is empty,startDate:{},bondCode:{}",startDate,bondCode);
            throw new CalcException("currencyValuation is empty");
        }
        log.info("the result of currencyValuation map :{}",map);
        return map;
    }

    /**
     * 查询所有债券最新的中债估值
     */
    public List<CurrencyValuation> getNewCurrencyValuationList(){
        List<CurrencyValuation> currencyValuationList = new ArrayList<>();
        Cursor<CurrencyValuation> cursor = null;
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession();
            CurrencyValuationMapper securityMasterMapper = sqlSession.getMapper(CurrencyValuationMapper.class);
            cursor = securityMasterMapper.selectNewCurrencyValuationList();
            cursor.forEach(currencyValuationList::add);
        }catch (Exception e){
            log.error("通过游标查询所有债券最新的中债估值失败！{}",e.getMessage());
        }finally {
            if (null != cursor) {
                try{
                    cursor.close();
                }catch (Exception e){
                    log.error("通过游标插叙浓缩铀债券最新的中债估值完成后，关闭游标失败！{}",e.getMessage());
                }
            }
            if (null != sqlSession) {
                try{
                    sqlSession.close();
                }catch (Exception e){
                    log.error("通过游标查询所有债券最新的中债估值完成后，关闭sqlSession失败1{}",e.getMessage());
                }
            }
        }
        return currencyValuationList;
    }

    /**
     * 创建今日中债估值物化视图
     */
    public int createMaterializedView(){
        return  this.currencyValuationMapper.createMaterializedView();
    }
    /**
     * 删除今日中债估值物化视图
     */
    public int deleteMaterializedView(){
        return this.currencyValuationMapper.deleteMaterializeView();
    }
    /**
     * 创建今日中债估值物化视图索引
     */
    public int createMaterializedViewIndex(){
        return  this.currencyValuationMapper.createMaterializedViewIndex();
    }


}
