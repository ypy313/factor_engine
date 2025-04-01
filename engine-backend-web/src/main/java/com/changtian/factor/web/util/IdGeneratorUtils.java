package com.changtian.factor.web.util;

/**
 * ID生成器
 *
 * @author wu.zhou
 * @date 2022-05-22 16:18
 */
public class IdGeneratorUtils {


    /**
     * 初始化SnowFlakeUtils
     */
    private final static SnowFlakeUtils snowFlake = new SnowFlakeUtils(2, 3);


    /**
     * 生成唯一值主键ID
     *
     * @return 结果
     */
    public static Long generatorId() {
        return snowFlake.nextId();
    }
}
