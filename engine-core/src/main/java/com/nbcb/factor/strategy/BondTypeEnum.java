package com.nbcb.factor.strategy;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 债券类型枚举
 */
@Getter
public enum BondTypeEnum {
    NATIONAL_DEBT("1232","国债","nationalDebtYield"),
    BANK_OF_CHINA_BOND("1322","口行债","bankOfChinaBondYield"),
    NATIONAL_DEVELOPMENT_BANK_BOND("2142","国开债","nationalDevelopmentBankBondYield"),
    AGRICULTURAL_DEVELOPMENT_BOND("2352","农发债","agriculturalDevelopmentBondYield");
    private String type;
    private String name;
    private String key;

    BondTypeEnum(String type, String name, String key) {
        this.type = type;
        this.name = name;
        this.key = key;
    }

    /**
     * 根据key获取枚举
     * @param key key
     * @return 结果
     */
    public static BondTypeEnum getBondTypeEnumByKey(String key) {
        for (BondTypeEnum value : BondTypeEnum.values()) {
            if (value.key.equals(key)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取所有的key
     * @return 集合
     */
    public static List<String> getBondTypeKeyList(){
        List<String> resultList = new ArrayList<>();
        for (BondTypeEnum value : BondTypeEnum.values()) {
            resultList.add(value.key);
        }
        return resultList;
    }
}
