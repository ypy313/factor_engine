package com.nbcb.factor.web.entity.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 参数配置表
 */
@Getter
@Setter
@ToString
public class SysConfig {
    private static final long serialVersionUID = 1L;
    //参数主键
    private Long configId;
    //参数名称
    private String configName;
    //参数键名
    private String configKey;
    //参数键值
    private String configValue;
    //系统内置（Y是 N否）
    private String configType;
    //创建者
    private Long createBy;
    //创建时间
    private Date createdTime;
    //更新者
    private Long updateBy;
    //更新时间
    private Date updateTime;
    //备注
    private String remark;

}
