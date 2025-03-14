package com.nbcb.factor.web.service;

import com.nbcb.factor.web.entity.user.SysUserEntity;
import com.nbcb.factor.web.mapper.SysFactorFavMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 因子关乎相关实力等数据查询
 */
@Service
public class FactorFavService {
    @Autowired
    private SysFactorFavMapper sysFactorFavMapper;
    /**
     * 通过实例id查询用户关注的工号id集合
     */
    public List<String> selectJobNumFavByInstance(String instanceId){
        return sysFactorFavMapper.selectJobNumFavByInstance(instanceId);
    }
    /**
     * 查询实例查询工作平台推送的时间区间
     */
    public List<SysUserEntity> selectJobWorkTimeByInstance(String instanceId){
        return sysFactorFavMapper.selectJobWorkTimeByInstance(instanceId);
    }
    /**
     * 通过实例查询工作平台推送时间区间
     */
    public SysUserEntity selectPushTimeByInstanceId(String instanceId){
        return sysFactorFavMapper.selectPushTimeByInstanceId(instanceId);
    }
}
