package com.nbcb.factor.common.key;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * 用于内部各缓存的key
 */
@Getter
@EqualsAndHashCode
public class MyKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String symbol;
    private final String period;
    private final String indexCategory;

    public MyKey(String symbol, String period, String indexCategory) {
        this.symbol = symbol;
        this.period = period;
        this.indexCategory = indexCategory;
    }

    @Override
    public String toString() {
        return symbol + '_' + period;
    }

    public String toAllString() {
        return symbol + '_' + period + "_" + indexCategory;
    }
}
