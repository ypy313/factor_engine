package com.changtian.factor.web.entity;

import lombok.Data;

import java.util.Date;

/**
 * 实例配置数据表
 */
@Data
public class FactorInstanceConfigData {
    //主键id
    private Long id;
    //配置信息id
    private Long configInfoId;
    //数据key
    private String dataKey;
    //数据名称
    private String dataName;
    //数据类型
    private String dataType;
    //默认值
    private String defauleValue;
    //是否是初始值Y-是 N-否
    private  String isInitialValue;
    //最小值
    private String minValue;
    //最大值
    private String maxValue;
    //显示排序
    private Integer displayOrder;
    //创建人
    private Long createdBy;
    //创建时间
    private Date createdTime;
    //更新人
    private Long updateBy;
    //更新时间
    private Date updateTime;

    private FactorInstanceConfigData(FactorInstanceConfigDataBuilder factorInstanceConfigDataBuilder){
        this.id = factorInstanceConfigDataBuilder.id;
        this.configInfoId = factorInstanceConfigDataBuilder.configInfoId;
        this.dataKey = factorInstanceConfigDataBuilder.dataKey;
        this.dataName = factorInstanceConfigDataBuilder.dataName;
        this.dataType = factorInstanceConfigDataBuilder.dataType;
        this.defauleValue = factorInstanceConfigDataBuilder.defalueValue;
        this.isInitialValue = factorInstanceConfigDataBuilder.isInitialValue;
        this.minValue = factorInstanceConfigDataBuilder.minValue;
        this.maxValue = factorInstanceConfigDataBuilder.maxValue;
        this.displayOrder = factorInstanceConfigDataBuilder.displayOrder;
        this.createdBy = factorInstanceConfigDataBuilder.createBy;
        this.createdTime = factorInstanceConfigDataBuilder.createTime;
        this.updateBy = factorInstanceConfigDataBuilder.updateBy;
        this.updateTime = factorInstanceConfigDataBuilder.updateTime;
    }

    public static class FactorInstanceConfigDataBuilder {
        //主键
        private Long id;
        //配置信息id
        private Long configInfoId;
        //数据KEY
        private String dataKey;
        //数据名称
        private String dataName;
        //数据类型
        private String dataType;
        //默认值
        private String defalueValue;
        //是否是初始值 Y-是 N-否
        private String isInitialValue;
        //最小值
        private String minValue;
        //最大值
        private String maxValue;
        //显示排序
        private Integer displayOrder;
        //创建人
        private Long createBy;
        //创建时间
        private Date createTime;
        //更新人
        private Long updateBy;
        //更新时间
        private Date updateTime;
        public FactorInstanceConfigDataBuilder setId(Long id) {
            this.id = id;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setConfigInfoId(Long configInfoId) {
            this.configInfoId = configInfoId;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setDataKey(String dataKey) {
            this.dataKey = dataKey;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setDataName(String dataName) {
            this.dataName = dataName;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setDataType(String dataType) {
            this.dataType = dataType;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setDefaultValue(String defaultValue) {
            this.defalueValue = defaultValue;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setIsInitialValue(String isInitialValue) {
            this.isInitialValue = isInitialValue;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setMinValue(String minValue) {
            this.minValue = minValue;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setMaxValue(String maxValue) {
            this.maxValue = maxValue;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setCreateBy(Long createBy) {
            this.createBy = createBy;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setCreateTime(Date createTime) {
            this.createTime = createTime;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setUpdateBy(Long updateBy) {
            this.updateBy = updateBy;
            return  this;
        }
        public FactorInstanceConfigDataBuilder setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
            return  this;
        }
        public FactorInstanceConfigData build(){
            return new FactorInstanceConfigData(this);
        }
    }
}
