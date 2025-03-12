package com.nbcb.factor.web.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SubscriptionAssetDataVo {
    private String symbol;//资产名称
    private String symbolType;//资产类型
}
