package com.changtian.factor.web.mapper;

import com.changtian.factor.web.entity.user.SysUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通过实例查询
 */
@Repository
public interface SysFactorFavMapper {
    /**
     * 通过实例id查询用户关乎的工号id集合
     */
    List<String> selectJobNumFavByInstance(String instanceId);
    /**
     * 通过实例id查询用户关注的工号id/用户名/工作平台推送区间
     */
    List<SysUserEntity> selectJobWorkTimeByInstance(String instanceId);
    /**
     * 通过实例id查询实例发送信号区间
     */
    SysUserEntity selectPushTimeByInstanceId(String instanceId);

}
