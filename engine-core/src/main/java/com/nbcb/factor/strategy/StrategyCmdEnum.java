package com.nbcb.factor.strategy;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 策略状态枚举
 */
@Getter
public enum StrategyCmdEnum {
    RELOAD("reload","重新加载"),//策略所属实例重新加载，用在新增或者修改实例配置的场景，作用范围是整个job
    SUSPEND("suspend","暂停"),//策略实例暂停，只做计算，不做输出，前置状态必须为正常。作用范围是单个策略实例
    RESUME("resume","恢复"),//策略实例恢复，状态变为正常。作用范围是单个策略实例
    START("start","启动"),//策略实例启动，对于状态为停止的策略实例，开始启动。作为范围是单个策略数据
    STOP("stop","停止"),//策略实例停止。对于正常状态的策略进行停止，停止状态的策略实例，不做计算，也不做输出。作用范围是单个策略实例
    ON_TIMER("onTimer","定时执行"),//定时执行 不重新加载因子实例
    DUMP("dump","打印"),//输出策略实例内存数据
    DATA_PROCESS_FINISH("dataProcessFinish","基础数据处理完成"),//基础数据处理完成
    DELETE("delete","删除");//外汇因子实例删除状态
    ;
    private final String cmd;//命令
    private final String desc;//描述

    StrategyCmdEnum(String cmd, String desc) {
        this.cmd = cmd;
        this.desc = desc;
    }

    /**
     * 校验策略命令是否合法
     * @param cmd 命令
     * @return 结果
     */
    public static boolean verifyStrategyCmd(String cmd){
        if (StringUtils.isNotBlank(cmd)) {
            for (StrategyCmdEnum value : StrategyCmdEnum.values()) {
                if (StringUtils.equals(cmd,value.cmd)) {
                    return true;
                }
            }
        }
        return false;
    }
}
