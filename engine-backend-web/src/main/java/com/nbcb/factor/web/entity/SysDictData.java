package com.nbcb.factor.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 字典数据实体
 */
@Data
public class SysDictData {
    //字典编码
    private Long dictCode;
    //字典排序
    private Long dictSort;
    //字典标签
    private String dictLabel;
    //字典键值
    private String dictValue;
    //字典类型
    private String dictType;
    //样式属性
    private String cssClass;
    //表格字典样式
    private String listClass;
    //是否默认（Y是 N否）
    private String isDefault;
    //状态（0 正常 1停用）
    private String status;
    //创建者
    private Long createBy;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Long updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    //备注
    private String remark;
}
