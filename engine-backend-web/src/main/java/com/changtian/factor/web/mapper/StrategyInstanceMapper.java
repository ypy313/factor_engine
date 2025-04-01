package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.ProjectTraStrategyInstanceVo;
import com.changtian.factor.web.entity.RateVo;
import com.changtian.factor.web.entity.RidingAssetPoolVo;
import com.changtian.factor.entity.BondCFVo;
import com.changtian.factor.entity.CbondCurveCnbdVo;
import com.changtian.factor.web.entity.SubscriptionAssetDataVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrategyInstanceMapper {
    /**
     * 查询本币实例
     */
    List<ProjectTraStrategyInstanceVo> getProjectTraStrategyInputVoList(String strategyName, Long strategyInstanceId, List<String> assetPoolId);
    /**
     * 查询外汇贵金属实例
     */
    List<ProjectTraStrategyInstanceVo> getPmTaProjectTraStrategyList(String startegyName,Long startegyInstanceId,List<String> assetPoolId );
    /**
     * 查询骑乘因子实例
     */
    List<RidingAssetPoolVo> getRidingYieldCurveInstanceList(String strategyName, List<Long> instacneIdList, List<String> assetPoolIdList);
    /**
     * 获取最新收益率曲线
     */
    List<CbondCurveCnbdVo> getRidingBondCurveCNBD();
    /**
     * 获取债券现金流
     */
    List<BondCFVo> getBondCFByBonds(String bonds);
    /**
     * 获取所有债券现金流
     */
    List<BondCFVo> getBondCFAll();
    /**
     * 查询shibor7d最新值
     */
    String selectLatestValueShiborPrices(@Param("sInfoWindCode") String sInfoWindCode);
    /**
     * 查询shibor7d均值
     */
    String  selectAverageValueShiborPrices(@Param("sInfoWindCode") String sInfoWindCode,@Param("tradeDt") String tradeDt);
    /**
     * 查询repor7d最新值
     */
    String selectLatestValueReporInfo(@Param("sInfoWindCode") String sInfoWindCode);
    /**
     * 查询repor7d均值
     */
    String selectAverageValueReporRate(@Param("sInfoWindCode") String sInfoWindCode,@Param("tradeDt") String tradeDt);
    /**
     * 查询最近一年shibor7d利率
     */
    List<RateVo> queryAverageValueShiborPrices(@Param("sInfoWindCode") String sInfoWindCode);
    /**
     * 查询最近一年repo7d利率
     */
    List<RateVo> queryAverageValueReporRate(@Param("sInfoWindCode") String sInfoWindCode);
    /**
     * 获取订阅行情数据
     */
    List<SubscriptionAssetDataVo> selectSubscriptionAssetData(String strategyName, String strategyInstanceId, String symbol, String symbolType);
}
