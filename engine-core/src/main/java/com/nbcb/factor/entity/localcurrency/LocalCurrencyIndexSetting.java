package com.nbcb.factor.entity.localcurrency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本币指标设置
 */
@Setter@Getter@ToString
public class LocalCurrencyIndexSetting {
    private String chiName;//参数解释
    private String indexName;//参数名称
    private int indexPreferences;//参数值

    /**
     * 设置chiName参数解释
     */
    public void chiNameBuild(LocalCurrencyIndexSetting localCurrencyIndexSetting){
        switch (localCurrencyIndexSetting.getIndexName()){
            case "ChinaBondSpreadPercentile":
                localCurrencyIndexSetting.setChiName("分位数周期");
                break;
            case "ChinaBondSpreadVolatility":
                localCurrencyIndexSetting.setChiName("波动率周期");
            case "RealTimeSpread":
                localCurrencyIndexSetting.setChiName("实时价差");
                break;
            default:break;
        }
    }
}
