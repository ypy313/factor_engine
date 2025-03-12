package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.po.RateSwapClosingCurvePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateSwapClosingCurveMapper {
    /**
     * 批量保存改日行情利率互换曲线收盘数据
     */
    int insertBatch(@Param("list") List<RateSwapClosingCurvePO> list);
    /**
     * 根据日期删除数据
     */
    int deleteByTradeDt(@Param("tradeDet") String tradeDt);
    /**
     * 查询日期条数
     */
    int selectNumByTradeDt(@Param("tradeDt") String tradeDt);

}
