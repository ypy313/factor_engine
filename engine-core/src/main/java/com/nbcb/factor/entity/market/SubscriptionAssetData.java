package com.nbcb.factor.entity.market;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 订阅行情数据
 */
@Setter@Getter@ToString
public class SubscriptionAssetData {
    private String symbol;//资产名称
    private String symbolType;//资产类型
}
