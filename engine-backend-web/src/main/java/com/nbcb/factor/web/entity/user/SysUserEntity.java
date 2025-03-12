package com.nbcb.factor.web.entity.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户配置实例类
 */
@Setter
@Getter
@ToString
public class SysUserEntity {
    private String jobId;
    private String userId;
    private String workStartTime;
    private String workEndTime;
    private String userName;
    private String pushStartTime;
    private String pushEndTime;
}
