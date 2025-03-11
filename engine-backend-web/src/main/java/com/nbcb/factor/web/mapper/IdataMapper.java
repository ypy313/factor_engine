package com.nbcb.factor.web.mapper;

import com.nbcb.factor.web.entity.dto.TimeConfigDto;

public interface IdataMapper {
    /**
     * 查询idata是否有拉取数据
     */
    int selectIdataDebtMarketCount();
    /**
     * 数据差量同步到INDVBOND_COUNTERPARTY_ANALYSIS
     */
    void mergeIndvBondCounterpartyAnalysis();
    /**
     * 数据差量同步到INDVBOND_LOAN_ANALYSIS
     */
    void mergeIndvBondLoanAnalysis();
    /**
     * 数据差量同步到INDVBOND_LOAN_ANALYSIS
     */
    void mergeIndvbondTradingAction();
    /**
     * 数据差量同步到BOND_LENDING_SUMMARY_ANALYSIS
     */
    void mergeBondLendingSummaryAnalysis();
    /**
     * 查询idata
     */
    int selectIdataOrgCount();
    /**
     * 数据差量同步到report_org_behavior
     */
    void mergeReportOrgBehavior();
    /**
     * 删除infa表和indvbondTradingAction
     */
    void deleteIndvbondTradingAction();
    /**
     * 删除infa表和IndvBondCounterpartyAnalysis都存在的数据
     */
    void deleteIndvBondCounterpartyAnalysis(TimeConfigDto timeConfigDto);
    /**
     * 删除infa 表和IndvBondLoanAnalysis都存在的数量
     */
    void  deleteIndvBondLoanAnalysis(TimeConfigDto timeConfigDto);
    /**
     * 删除infa 表 和BondlendingSummaryAnalysis 都存在的数据
     */
    void deleteBondLendingSummaryAnalysis(TimeConfigDto timeConfigDto);
    /**
     * 删除infa表和ReporOrgBehavior都存在的数据
     */
    void deleteReportOrgBehavior(TimeConfigDto timeConfigDto);
    /**
     * 创建IndvBondCounterpartyAnalysis临时表
     */
    void createTempleIndvBondCounterpartyAnalysis();
    /**
     * 将IndvBondCounterpartyAnalysis
     */
    void insertTempIndvBondCounterpartyAnalysis();
    /**
     * 清理IndvBondCounterpartyAnalysis
     */
    void truncateIndvBondCounterpartyAnalysis();
    /**
     * 将临时表插入 IndvBondCounterpartyAnalysis
     */
    void insertIndvBondCounterpartyAnalysis();
    /**
     * 删除临时表
     */
    void dropTempIndvBondCounterpartyAnalysis();
    /**
     * 创建临时表
     */
    void createTempIndvbondTradingAction();
    /**
     * 将IndvbondTradingAction插入临时表
     */
    void insertTempIndvbondTradingAction();
    /**
     * 清理IndvbondTradingAction
     */
    void  truncateIndvbondTradingAction();
    /**
     * 将临时表 插入IndvbondTradingAction
     */
    void  insertIndvbondTradingAction();
    /**
     * 删除临时表
     */
    void dropTempIndvbondTradingAction();
    /**
     * 创建临时表
     */
    void createTempIndvBondLoanAnalysis();
    /**
     * 将IndvBondLoanAnalysis插入临时表
     */
    void insertTempIndvBondLoanAnalysis();
    /**
     * 清理indvBondLoanAnalysis
     */
    void truncateIndvBondLoanAnalysis();
    /**
     * 将临时表插入 IndvBondLoanAnalysis
     */
    void insertIndvBondLoanAnalysis();
    /**
     * 删除临时表
     */
    void dropTempIndvBondLoanAnalysis();
    /**
     * 创建临时表
     */
    void createTempBondLendingSummaryAnalysis();
    /**
     * 将bondLendingSummaryAnalysis 插入临时表
     */
    void insertTempBondLendingSummaryAnalysis();
    /**
     * 清理BondLenddingSummaryAnalysis
     */
    void truncateBondLendingSummaryAnalysis();
    /**
     * 将临时表插入BondLendingSummaryAnalysis
     */
    void insertBondLendingSummaryAnalysis();
    /**
     * 删除临时表
     */
    void dropTempBondLendingSummaryAnalysis();
    /**
     * 创建临时表
     */
    void createTempReportOrgBehavior();
    /**
     * 将reporOrgBehavior 插入临时表
     */
    void insertTempReportOrBehavior();
    /**
     * 清理ReporOrgBehavior
     */
    void truncateReportOrgBehavior();
    /**
     * 将临时表 插入ReportOrgBehavior
     */
    void insertReportOrgBehavior();
    /**
     * 删除临时表
     */
    void dropTempReportOrgBehavior();

}
