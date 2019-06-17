/**
 *    Copyright 2017-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.econage.core.db.mybatis.entity;

import com.econage.core.db.mybatis.enums.FieldStrategy;
import com.econage.core.db.mybatis.annotations.TableField;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.econage.core.db.mybatis.util.MybatisStringUtils;

import java.lang.reflect.Field;


/**
 * <p>
 * 数据库表字段反射信息
 * </p>
 */
public class TableFieldInfo {
    public static TableFieldInfo newTableFieldInfoByAnnotation(
            MybatisGlobalAssistant globalAssistant,
            Field fieldInfo,
            TableField tableFieldAnnotation
    ){
        TableFieldInfo tableFieldInfo = new TableFieldInfo();
        tableFieldInfo.setAutoMappingColumn(globalAssistant.formatColumn(fieldInfo.getName()));

        if (MybatisStringUtils.isNotEmpty(tableFieldAnnotation.value())) {
            tableFieldInfo.setAutoMapping(false);
            tableFieldInfo.setColumn(tableFieldAnnotation.value());
        }else{
            tableFieldInfo.setAutoMapping(true);
            tableFieldInfo.setColumn(tableFieldInfo.getAutoMappingColumn());
        }

        tableFieldInfo.setProperty(fieldInfo.getName());
        tableFieldInfo.setPropertyType(fieldInfo.getType());

        if(MybatisStringUtils.isNotEmpty(tableFieldAnnotation.el())){
            tableFieldInfo.setEl(tableFieldAnnotation.el());
        }else{
            tableFieldInfo.setEl(tableFieldInfo.getProperty());
        }

        if (FieldStrategy.NOT_NULL != tableFieldAnnotation.strategy()) {
            tableFieldInfo.setFieldStrategy(tableFieldAnnotation.strategy());
        } else {
            tableFieldInfo.setFieldStrategy(globalAssistant.getDefaultFieldStrategy());
        }
        //如果有注解，则字段以注解为准
        tableFieldInfo.setDefaultInsert(tableFieldAnnotation.defaultInsert());
        tableFieldInfo.setDefaultUpdate(tableFieldAnnotation.defaultUpdate());

        //如果某个字段是外键字段
        tableFieldInfo.setFk(tableFieldAnnotation.isFk());
        //如果某个字段是乐观锁字段
        tableFieldInfo.setVersion(tableFieldAnnotation.isVersion());
        //树形结构，父节点关系列
        tableFieldInfo.setTreeParentLink(tableFieldAnnotation.isTreeParentLink());
        //树形结构，排序列
        tableFieldInfo.setTreeSiblingOrder(tableFieldAnnotation.isTreeSiblingOrder());

        return tableFieldInfo;
    }
    public static TableFieldInfo newTableFieldInfo(
            MybatisGlobalAssistant globalAssistant,
            Field field
    ){
        TableFieldInfo tableFieldInfo = new TableFieldInfo();
        tableFieldInfo.setAutoMappingColumn(globalAssistant.formatColumn(field.getName()));
        tableFieldInfo.setAutoMapping(true);
        tableFieldInfo.setColumn(tableFieldInfo.getAutoMappingColumn());
        tableFieldInfo.setProperty(field.getName());
        tableFieldInfo.setPropertyType(field.getType());
        tableFieldInfo.setEl(field.getName());
        tableFieldInfo.setFieldStrategy(globalAssistant.getDefaultFieldStrategy());
        //如果没有注解，并且某个属性名是保留的名称的，类似createDate，则不在默认更新方法中使用
        tableFieldInfo.setDefaultUpdate(globalAssistant.enableInDefaultUpdateMethod(tableFieldInfo.getProperty()));
        return tableFieldInfo;
    }


    /*
     * false 字段是通过注解填充的，
     * true  通过解析属性名称解析的
     * */
    private boolean autoMapping;
    private String autoMappingColumn;

    /*
     * 是否是外键
     * */
    private boolean fk;

    /*
    * 是否是乐观锁字段
    * */
    private boolean version;

    /*
    * 是否树形关系父节点关系列
    * */
    private boolean treeParentLink;
    /*
    * 树形关系，节点排序列
    * */
    private boolean treeSiblingOrder;

    /**
     * 字段名
     */
    private String column;

    /**
     * 属性名
     */
    private String property;

    /**
     * 属性表达式#{property}, 可以指定jdbcType, typeHandler等
     */
    private String el;
    /**
     * 属性类型
     */
    private Class<?> propertyType;

    /**
     * 字段策略【 默认，自判断 null 】
     */
    private FieldStrategy fieldStrategy = FieldStrategy.NOT_NULL;

    /*
    * 默认插入方法中使用
    * */
    private boolean defaultInsert = true;
    /*
    * 默认更新方法中使用
    * */
    private boolean defaultUpdate = true;

    /**
     * <p>
     * 存在 TableField 注解构造函数
     * </p>
     */
    private TableFieldInfo() {
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getEl() {
        return el;
    }

    public void setEl(String el) {
        this.el = el;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    private void setPropertyType(Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    public FieldStrategy getFieldStrategy() {
        return fieldStrategy;
    }

    private void setFieldStrategy(FieldStrategy fieldStrategy) {
        this.fieldStrategy = fieldStrategy;
    }

    public boolean isAutoMapping() {
        return autoMapping;
    }

    private void setAutoMapping(boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public String getAutoMappingColumn() {
        return autoMappingColumn;
    }

    private void setAutoMappingColumn(String autoMappingColumn) {
        this.autoMappingColumn = autoMappingColumn;
    }

    public boolean isDefaultInsert() {
        return defaultInsert;
    }

    private void setDefaultInsert(boolean defaultInsert) {
        this.defaultInsert = defaultInsert;
    }

    public boolean isDefaultUpdate() {
        return defaultUpdate;
    }

    private void setDefaultUpdate(boolean defaultUpdate) {
        this.defaultUpdate = defaultUpdate;
    }

    public boolean isFk() {
        return fk;
    }

    private void setFk(boolean fk) {
        this.fk = fk;
    }

    public boolean isVersion() {
        return version;
    }

    private void setVersion(boolean version) {
        this.version = version;
    }

    public boolean isTreeParentLink() {
        return treeParentLink;
    }

    public void setTreeParentLink(boolean treeParentLink) {
        this.treeParentLink = treeParentLink;
    }

    public boolean isTreeSiblingOrder() {
        return treeSiblingOrder;
    }

    public void setTreeSiblingOrder(boolean treeSiblingOrder) {
        this.treeSiblingOrder = treeSiblingOrder;
    }
}
