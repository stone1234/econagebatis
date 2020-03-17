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

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.econage.core.db.mybatis.annotations.KeySequence;
import com.econage.core.db.mybatis.annotations.TableDef;
import com.econage.core.db.mybatis.annotations.TableField;
import com.econage.core.db.mybatis.annotations.TableId;
import com.econage.core.db.mybatis.enums.IdType;
import com.econage.core.db.mybatis.util.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 实体类反射表辅助类
 * </p>
 */
public class MybatisTableInfoHelper {

    private static final Log logger = LogFactory.getLog(MybatisTableInfoHelper.class);
    /**
     * 默认表主键
     */
    private static final String DEFAULT_ID_PROPERTY = "id";

    /**
     * <p>
     * 实体类反射获取表信息【初始化】
     * <p>
     */
    public static TableInfo parseTableInfo(
            MybatisGlobalAssistant globalAssistant,
            Class<?> modelClass
    ) {
        MybatisPreconditions.checkNotNull(modelClass,"model class is null!");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setModelClass(modelClass);
        /* 表名 */
        TableDef table = modelClass.getAnnotation(TableDef.class);
        String tableName = modelClass.getSimpleName();
        if (table != null && MybatisStringUtils.isNotEmpty(table.value())) {
            tableName = table.value();
        } else {
            tableName = globalAssistant.formatTableName(tableName);
        }
        tableInfo.setTableName(tableName);

        // 开启了自定义 KEY 生成器
        if (null != globalAssistant.getKeyGenerator()) {
            tableInfo.setKeySequence(modelClass.getAnnotation(KeySequence.class));
        }

        /* 表结果集映射 */
        if (table != null && MybatisStringUtils.isNotEmpty(table.defaultSelectResultMap())) {
            tableInfo.setDefaultSelectResultMap(table.defaultSelectResultMap());
        }
        List<TableFieldInfo> fieldList = new ArrayList<>();
        List<Field> list = getAllFields(globalAssistant,modelClass);
        // 标记是否读取到主键
        boolean isReadPK = false;
        boolean existTableId = existTableId(list);
        for (Field field : list) {
            //主键ID
            if (!isReadPK) {
                if (existTableId) {
                    isReadPK = initTableId(globalAssistant, tableInfo, field, modelClass);
                } else {
                    isReadPK = initFieldId(globalAssistant, tableInfo, field, modelClass);
                }
                if (isReadPK) {
                    continue;
                }
            }

            //尝试通过注解解析
            TableFieldInfo tableFieldInfo = parseTableFieldByAnnotation(globalAssistant, field, modelClass);
            if (tableFieldInfo==null) {
                //如果没有注解，直接解析
                tableFieldInfo = TableFieldInfo.newTableFieldInfo(globalAssistant, field);
            }
            //如果某个字段是外键
            if(tableFieldInfo.isFk()){
                tableInfo.setFkField(tableFieldInfo);
            }
            //如果某个字段是乐观锁字段
            if(tableFieldInfo.isVersion()){
                tableInfo.setVersionField(tableFieldInfo);
            }
            if(tableFieldInfo.isTreeParentLink()){
                tableInfo.setTreeParentLinkField(tableFieldInfo);
            }
            if(tableFieldInfo.isTreeSiblingOrder()){
                tableInfo.setTreeSiblingOrderField(tableFieldInfo);
            }
            fieldList.add(tableFieldInfo);
        }

		/* 字段列表 */
        tableInfo.setFieldList(fieldList);

        tableInfo.setSelectColumns(sqlSelectColumns(tableInfo));

        /*
         * 未发现主键注解，提示警告信息
		 */
        if (MybatisStringUtils.isEmpty(tableInfo.getKeyColumn())) {
            logger.warn(String.format("Warn: Could not find @TableId in Class: %s.", modelClass.getName()));
        }
        return tableInfo;
    }

    /**
     * <p>
     * 判断主键注解是否存在
     * </p>
     *
     * @param list 字段列表
     * @return
     */
    public static boolean existTableId(List<Field> list) {
        boolean exist = false;
        for (Field field : list) {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    /**
     * <p>
     * 主键属性初始化
     * </p>
     *
     * @param tableInfo
     * @param field
     * @param clazz
     * @return true 继续下一个属性判断，返回 continue;
     */
    private static boolean initTableId(MybatisGlobalAssistant globalAssistant, TableInfo tableInfo, Field field, Class<?> clazz) {
        TableId tableId = field.getAnnotation(TableId.class);
        if (tableId != null) {
            if(MybatisStringUtils.isNotEmpty(tableInfo.getKeyProperty())){
                throwExceptionId(clazz);
            }

            /*
            * 主键策略（ 注解 > 全局 > 默认 ）
			*/
            // 设置 Sequence 其他策略无效
            if (IdType.NONE != tableId.type()) {
                tableInfo.setIdType(tableId.type());
            } else if(tableInfo.getKeySequence()==null) {
                //如果未设置idtype，并且为设置keySequence，则以全局配置为准
                tableInfo.setIdType(globalAssistant.getDefaultIdType());
            }

            tableInfo.setKeyProperty(field.getName());
            tableInfo.setKeyAutoMappingColumn(globalAssistant.formatColumn(tableInfo.getKeyProperty()));
            if(MybatisStringUtils.isNotEmpty(tableId.value())){
                tableInfo.setKeyColumn(MybatisSqlReservedWords.convert(globalAssistant,tableId.value()));
            }else{
                tableInfo.setKeyAutoMapping(true);
                tableInfo.setKeyColumn(tableInfo.getKeyAutoMappingColumn());
            }
            return true;
        }
        return false;
    }

    /**
     * <p>
     * 主键属性初始化
     * </p>
     *
     * @param tableInfo
     * @param field
     * @param clazz
     * @return true 继续下一个属性判断，返回 continue;
     */
    private static boolean initFieldId(MybatisGlobalAssistant globalAssistant, TableInfo tableInfo, Field field, Class<?> clazz) {
        String property = field.getName();
        if (DEFAULT_ID_PROPERTY.equalsIgnoreCase(property)) {
            if (MybatisStringUtils.isEmpty(tableInfo.getKeyProperty())) {
                if(tableInfo.getKeySequence()==null) {
                    //如果未设置idType，并且未设置keySequence，则以全局配置为准
                    tableInfo.setIdType(globalAssistant.getDefaultIdType());
                }
                tableInfo.setKeyProperty(property);
                tableInfo.setKeyAutoMapping(true);
                tableInfo.setKeyAutoMappingColumn(globalAssistant.formatColumn(property));
                tableInfo.setKeyColumn(tableInfo.getKeyAutoMappingColumn());
                return true;
            } else {
                throwExceptionId(clazz);
            }
        }
        return false;
    }

    /**
     * <p>
     * 发现设置多个主键注解抛出异常
     * </p>
     */
    private static void throwExceptionId(Class<?> clazz) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("There must be only one, Discover multiple @TableId annotation in ");
        errorMsg.append(clazz.getName());
        throw new MybatisException(errorMsg.toString());
    }

    /**
     * <p>
     * 字段属性初始化
     * </p>
     *
     * @param globalAssistant 全局配置
     * @param clazz        当前表对象类
     * @return true 继续下一个属性判断，返回 continue;
     */
    private static TableFieldInfo parseTableFieldByAnnotation(
            MybatisGlobalAssistant globalAssistant,
            Field field, Class<?> clazz
    ) {
        /* 获取注解属性，自定义字段 */
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null) {
            return TableFieldInfo.newTableFieldInfoByAnnotation(
                    globalAssistant,
                    field, tableField
            );
        }
        return null;
    }

    /**
     * 获取该类的所有属性列表
     *
     * @param modelClass 反射类
     * @return
     */
    private static List<Field> getAllFields(MybatisGlobalAssistant globalAssistant,Class<?> modelClass) {
        TypeHandlerRegistry typeHandlerRegistry =globalAssistant.getConfiguration().getTypeHandlerRegistry();
        List<Field> fieldList = MybatisReflectionKit.getFieldList(MybatisClassUtils.getUserClass(modelClass));
        if (MybatisCollectionUtils.isNotEmpty(fieldList)) {
            Iterator<Field> iterator = fieldList.iterator();
            while (iterator.hasNext()) {
                Field field = iterator.next();
                TableField tableField = field.getAnnotation(TableField.class);
                if(!typeHandlerRegistry.hasTypeHandler(field.getType())){
                    //mybatis框架没有转换逻辑的
                    iterator.remove();
                }else if (tableField != null && !tableField.exist()) {
                    /* 过滤注解非表字段属性 */
                    iterator.remove();
                }
            }
        }
        return fieldList;
    }

    private static String sqlSelectColumns(TableInfo tableInfo) {
        List<String> columnList = new ArrayList<>();
        if(tableInfo.getDefaultSelectResultMap()!=null){
            columnList.add("*");
        }else{
            // 主键处理
            if (MybatisStringUtils.isNotEmpty(tableInfo.getKeyProperty())) {
                if(tableInfo.isKeyAutoMapping()){
                    columnList.add(tableInfo.getKeyColumn());
                }else{
                    columnList.add(tableInfo.getKeyColumn()+" AS "+tableInfo.getKeyAutoMappingColumn());
                }
            }

            if(tableInfo.getFieldList()!=null){
                for(TableFieldInfo tableFieldInfo : tableInfo.getFieldList()){
                    if(tableFieldInfo.isAutoMapping()){
                        columnList.add(tableFieldInfo.getColumn());
                    }else{
                        columnList.add(tableFieldInfo.getColumn()+" AS "+tableFieldInfo.getAutoMappingColumn());
                    }
                }
            }

        }

        return MybatisSqlUtils.commaJoin(columnList);
    }
}
