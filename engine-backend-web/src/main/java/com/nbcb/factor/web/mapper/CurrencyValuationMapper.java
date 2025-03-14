package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.CurrencyValuation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

/**
 * 中债估值Mapper接口
 */
public interface CurrencyValuationMapper {

    List<CurrencyValuation> selectCurrencyValuationList(@Param("startDate") String startData, @Param("endDate") String endDate,
                                                        @Param("securityId1") String securityId1, @Param("securityId2") String securityId2);

    CurrencyValuation getCurrencyValuation(@Param("startDate") String startDate, @Param("securityId") String securityId);

    /**
     * 创建今日中债估值物化视图
     */
    int createMaterializedView();

    /**
     * 删除今日中债估值物化视图
     */
    int deleteMaterializeView();
    /**
     * 创建今日中债估值物化视图索引
     */
    int createMaterializedViewIndex();
    /**
     * 查询最新的中债估值
     */
    Cursor<CurrencyValuation> selectNewCurrencyValuationList();

}
