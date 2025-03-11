package com.nbcb.factor.web.mapper;

import com.nbcb.factor.entity.riding.Cbondanalysiscnbd;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 中债估值mapper
 */
@Repository
public interface CbondanalysiscnbdMapper {
    /**
     * 备份数据insert into
     */
    int backupCopyCbondanalysiscnbd(@Param("dateTime") String dateTime);
    /**
     * 获取需备份的条数
     */
    int getCbondanalysiscnbdCount(@Param("dateTime") String dateTime);
    /**
     * 执行删除数据
     */
    int deleteCbondanalysiscnbd(@Param("dateTime") String dateTime);
    /**
     * 查询所有债券最新利率
     */
    List<Cbondanalysiscnbd> selectBondRealTimeRateAll();

    Cbondanalysiscnbd getBondRealTimeRate(String bonds);
}
