package com.nbcb.factor.web.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RateSwapClosingCurveMapper {
    /**
     * 批量保存改日行情利率互换曲线收盘数据
     */
    int insertBatch(@Param("list") List<RateSwapClosingCurvePO> list);
}
