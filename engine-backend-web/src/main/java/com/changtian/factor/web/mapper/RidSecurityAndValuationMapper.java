package com.changtian.factor.web.mapper;

import com.changtian.factor.output.bondprice.BondBasisStaticData;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * rdi债券类型与中债估值数据mapper
 */
@Repository
public interface RidSecurityAndValuationMapper {
    List<BondBasisStaticData> selectAllRidSecurityValuation();
}
