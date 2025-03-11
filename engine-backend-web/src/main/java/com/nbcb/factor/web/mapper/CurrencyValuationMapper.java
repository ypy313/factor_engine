package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.CurrencyValuation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中债估值Mapper接口
 */
public interface CurrencyValuationMapper {
    List<CurrencyValuation> selectCurrencyValuationList(@Param("startDate") String startData,@Param("endDate") String endDate,
                                                        @Param("securityId1") String securityId1,@Param("securityId2") String securityId2);
}
