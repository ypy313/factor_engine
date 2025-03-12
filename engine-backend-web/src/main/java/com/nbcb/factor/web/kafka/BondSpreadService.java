package com.nbcb.factor.web.kafka;

/**
 * kafka数据处理
 */
public interface BondSpreadService {
    void process(String jsonStr);
}
