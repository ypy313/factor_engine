package com.changtian.factor.entity.forex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户撇脂的因子实例全局参数
 */
@Getter@Setter@ToString
public class GlobalParameter {
    private String parameterName;//参数名称
    private BigDecimal parameterValue;//参数值
    private int parameterOrder;//排序
    private String indexCategory;//指标类型
    private Date xpoint1;//x点1
    private BigDecimal ypoint1;//y点1
    private Date xpoint2;//x点2
    private BigDecimal ypoint2;//y点2
    private int x2Num;//在该货币对周期中x2点到x1点的bar个数
    private int x3Num;//在该货币对周期中当前时间到x1点的bar个数
    private BigDecimal xcell1;//x1下标值
    private BigDecimal xcell2;//x2下标值
    private Date latestXPoint;//点数图中倒数第二个行情点下表数据
    private BigDecimal latestXCell;//x1下标值
}
