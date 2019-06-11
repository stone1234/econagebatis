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

import com.econage.core.db.mybatis.annotations.KeySequence;
import com.econage.core.db.mybatis.enums.IdType;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/*
* tableInfo可能使用到的场景
* 1，继承了basemapper的mapper类默认方法中动态生成sql
* 2，mapper关联的sqlprovider类中使用，此时tableinfo可能没有，需要通过modelClass获取tableInfo
* 3，分页插件。分页插件解析到一个非mapper型的modelClass时，会尝试解析他，此时mapper类与model类会不一致
* */
public class TableInfo {

    /**
     * 表主键ID 类型
     */
    private IdType idType = IdType.NONE;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表映射结果集
     */
    private String defaultSelectResultMap;

    /**
     * <p>
     * 主键是否有存在字段名与属性名关联
     * </p>
     */
    private boolean keyAutoMapping;

    private String keyAutoMappingColumn;

    /**
     * 表主键ID 属性名
     */
    private String keyProperty;

    /**
     * 表主键ID 字段名
     */
    private String keyColumn;

    /**
     * <p>
     * 表主键ID Sequence
     * </p>
     */
    private KeySequence keySequence;

    /**
     * 表字段信息列表
     */
    private List<TableFieldInfo> fieldList;
    /*
     * 外键字段
     * */
    private TableFieldInfo fkField;
    /*
    * 充当乐观锁的字段
    * */
    private TableFieldInfo versionField;
    /*
    * 树形关系，父节点列
    * */
    private TableFieldInfo treeParentLinkField;
    /*
     * 树形关系，排序列
     * */
    private TableFieldInfo treeSiblingOrderField;

    /* 在加载fieldList时,刷新propertyFieldMap */
    private Map<String,TableFieldInfo> propertyFieldMap;
    /*
    * select 语句默认使用的列名，方便某些场景下写sql语句
    * */
    private String selectColumns;

    /*
     * 关联的model类信息
     * */
    private Class<?> modelClass;

    public IdType getIdType() {
        return idType;
    }

    void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getTableName() {
        return tableName;
    }

    void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDefaultSelectResultMap() {
        return defaultSelectResultMap;
    }

    void setDefaultSelectResultMap(String defaultSelectResultMap) {
        this.defaultSelectResultMap = defaultSelectResultMap;
    }

    public boolean isKeyAutoMapping() {
        return keyAutoMapping;
    }

    void setKeyAutoMapping(boolean keyAutoMapping) {
        this.keyAutoMapping = keyAutoMapping;
    }

    public String getKeyAutoMappingColumn() {
        return keyAutoMappingColumn;
    }

    void setKeyAutoMappingColumn(String keyAutoMappingColumn) {
        this.keyAutoMappingColumn = keyAutoMappingColumn;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public KeySequence getKeySequence() {
        return keySequence;
    }

    void setKeySequence(KeySequence keySequence) {
        this.keySequence = keySequence;
    }

    public List<TableFieldInfo> getFieldList() {
        return fieldList;
    }


    private static Function<TableFieldInfo,String> fetchFieldProperty = input-> {
            if(input!=null){
                return input.getProperty();
            }
            return null;
        };

    void setFieldList(List<TableFieldInfo> fieldList) {
        this.fieldList = ImmutableList.copyOf(fieldList);
        this.propertyFieldMap = Maps.uniqueIndex(fieldList,fetchFieldProperty);
    }

    public String getAutoMappingColumnByProperty(String property){
        Preconditions.checkNotNull(property,"property is null!");
        if(!propertyFieldMap.containsKey(property)){
            return null;
        }else if(keyProperty!=null&&keyProperty.equals(property)){
            return keyColumn;
        }
        return propertyFieldMap.get(property).getAutoMappingColumn();
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    void setModelClass(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public String getSelectColumns() {
        return selectColumns;
    }

    void setSelectColumns(String selectColumns) {
        this.selectColumns = selectColumns;
    }

    public TableFieldInfo getVersionField() {
        return versionField;
    }

    void setVersionField(TableFieldInfo versionField) {
        this.versionField = versionField;
    }

    public TableFieldInfo getFkField() {
        return fkField;
    }

    void setFkField(TableFieldInfo fkField) {
        this.fkField = fkField;
    }

    public String getFkProperty() {
        if(fkField!=null){
            return fkField.getProperty();
        }
        return StringUtils.EMPTY;
    }

    public String getFkColumn(){
        if(fkField!=null){
            return fkField.getColumn();
        }
        return StringUtils.EMPTY;
    }

    public TableFieldInfo getTreeParentLinkField() {
        return treeParentLinkField;
    }

    void setTreeParentLinkField(TableFieldInfo treeParentLinkField) {
        this.treeParentLinkField = treeParentLinkField;
    }

    public String getTreeParentLinkProperty() {
        if(treeParentLinkField!=null){
            return treeParentLinkField.getProperty();
        }
        return StringUtils.EMPTY;
    }

    public String getTreeParentLinkColumn(){
        if(treeParentLinkField!=null){
            return treeParentLinkField.getColumn();
        }
        return StringUtils.EMPTY;
    }

    public TableFieldInfo getTreeSiblingOrderField() {
        return treeSiblingOrderField;
    }

    void setTreeSiblingOrderField(TableFieldInfo treeSiblingOrderField) {
        this.treeSiblingOrderField = treeSiblingOrderField;
    }

    public String getTreeSiblingOrderProperty() {
        if(treeSiblingOrderField!=null){
            return treeSiblingOrderField.getProperty();
        }
        return StringUtils.EMPTY;
    }

    public String getTreeSiblingOrderColumn(){
        if(treeSiblingOrderField!=null){
            return treeSiblingOrderField.getColumn();
        }
        return StringUtils.EMPTY;
    }
}
