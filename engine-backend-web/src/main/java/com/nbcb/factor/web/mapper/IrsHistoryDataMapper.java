package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.IrsHistoryData;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * IRS历史数据Mapper
 */
@Repository
public interface IrsHistoryDataMapper {
    /**
     * 插入IRS历史数据
     */
    int insertIrsHistoryData(IrsHistoryData irsHistoryData);
    /**
     * 删除当前日期的数据
     */
    int deleteIrsHistoryData(String interestRate,String swapTerm,String effDate);
    /**
     * 调用存储过程更新利率
     */
    void callUpdateCaseFlowRate(String updateDate);
    /**
     * 查询最新利率_期限对应的起息日
     */
    List<IrsHistoryData> selectNowValueDate();
}
