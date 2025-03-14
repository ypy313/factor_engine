package com.nbcb.factor.web.service;

import com.nbcb.factor.web.entity.IrsHistoryData;
import com.nbcb.factor.web.mapper.IrsHistoryDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IRS历史数据service
 */
@Slf4j
@Service("irsHistoryDataService")
@Transactional(rollbackFor = Exception.class)
public class IrsHistoryDataService {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 批量插入IRS历史数据
     */
    public int batchInsertIrsHistoryData(List<IrsHistoryData> historyDataList) {
        //开启批量插入
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            IrsHistoryDataMapper irsHistoryDataMapper = session.getMapper(IrsHistoryDataMapper.class);
            historyDataList.forEach(irsHistoryDataMapper::insertIrsHistoryData);
            session.commit();
        } catch (Exception e) {
            log.error("批量插入IRS历史数据异常", e);
        }
        return historyDataList.size();
    }

    /**
     * 批量删除起息日数据
     */
    public void deleteIrsHistoryData(String interestRate, String swapTerm, String effDate) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            IrsHistoryDataMapper irsHistoryDataMapper = session.getMapper(IrsHistoryDataMapper.class);
            irsHistoryDataMapper.deleteIrsHistoryData(interestRate, swapTerm, effDate);
            session.commit();
        }catch (Exception e) {
            log.error("批量删除参考利率：{}，起息日：{} 的IRS历史数据异常",interestRate,effDate,e);
        }
    }

    /**
     * 调用存储过程更新利率
     */
    public void callUpdateCaseFlowRate(String updateDate){
        try(SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)){
            IrsHistoryDataMapper irsHistoryDataMapper = session.getMapper(IrsHistoryDataMapper.class);
            irsHistoryDataMapper.calUpdateCaseFlowRate(updateDate);
            session.commit();
        }catch (Exception e) {
            log.error("更新：{}历史数据定盘利率异常！",updateDate,e);
        }
    }
    /**
     * 查最新参看利率_期限对应的起息日
     */
    public Map<String ,String> selectNowValueDate(){
        Map<String,String> resultMap = new HashMap<>();
        try(SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)){
            IrsHistoryDataMapper irsHistoryDataMapper = session.getMapper(IrsHistoryDataMapper.class);
            List<IrsHistoryData> dataList = irsHistoryDataMapper.selectNowValueDate();
            if(!CollectionUtils.isEmpty(dataList)){
                dataList.forEach(d->resultMap.put(d.getReferenceRate()+"_"+d.getSwapTerm(),d.getValueDate()));
            }
            session.commit();
        }catch (Exception e) {
            log.error("查询最新参看利率_期限对应的起息日！",e);
        }
        return resultMap;
    }
}
