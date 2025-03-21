package com.nbcb.factor.web.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 骑乘与数据库交互相关
 */
@Repository
public interface RidingYieldCurveMapper {
    /**
     * 插入债券信息表
     */
    int selectCbonddescriptionInfa(@Param("todayStr") String todayStr);
    /**
     * 清空债券信息
     */
    int truncateCbonddescription();
    /**
     * 插入债券信息表
     */
    int insertCbonddescription(@Param("todayStr") String todayStr);
    /**
     * 删除昨日更新的现金流信息
     */
    int deleteCbondCf(@Param("yesterdayStr") String yesterdayStr);
    /**
     * 插入昨日更新的现金流信息
     */
    int insertCbondCf(@Param("yesterdayStr") String yesterdayStr);
    /**
     * 记录infa增量的所有数据
     */
    int insertCbondCfInfaHis(@Param("yesterdayStr") String yesterdayStr);
    /**
     * 同花顺改造添加 查询现金流是否有拉取数据
     */
    int selectCBONDCF();
    /**
     * 插入收益率曲线表
     */
    int insertCbondCurveCNBD(@Param("yesterdayStr")String yesterdayStr);
    /**
     * 删除昨日新增回购repo利率表
     */
    int deleteCbondInBankRateCfets();
    /**
     * 新增昨日新增回购（repo）利率表
     */
    int insertCbondInBankRateCfets();
    /**
     * 珊瑚昨日shibor利率
     */
    int deleteShiborprices();
    /**
     * 新增昨日shibor利率表
     */
    int insertShiborprices();
    /**
     * 清空现金流cbondcf表数据
     */
    void truncateCbondCf();
    /**
     * 全量插入现金流cbondcf
     */
    int insertAllCbondCfByCbonddescription();
    /**
     * 删除repo利率表CBONDINTERBANKMARKETRATECFETS数据
     */
    void truncateCBONDINTERBANKMARKETRATECFETS();
    /**
     * 清理shibor利率表shiborpricees数据
     */
    void truncateShiborprices();
    /**
     * 查询需要插入的repo利率条数
     */
    int selectInsertCountAboutRepo();
    /**
     * 查询需要插入的repo利率条数
     */
    int selectInsertCountAboutShibor();
}
