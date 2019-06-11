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
package com.econage.core.db.mybatis.mapper.defaultsqlsource.basic;

import com.econage.core.db.mybatis.entity.TableFieldInfo;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.uuid.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultUpdateMethodSqlSource extends AbstractDefaultMethodSqlSource {

    public static final String UPDATE_SQL_TEMPLATE = "UPDATE %s SET %s WHERE %s=#{%s}";

    private final boolean selective;
    private final boolean partial;

    public DefaultUpdateMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo,
            boolean selective, boolean partial
    ) {
        super(configuration, tableInfo);
        this.selective = selective;
        this.partial = partial;
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(selective&&partial){
            throw new IllegalStateException("selective and partial both true!");
        }
        /*---------------------1，解析参数*/
        Collection<String> validProperty = null;
        if(partial){
            Map<String, Object> params = (Map<String, Object>) parameterObject;
            parameterObject = params.get("et");
            validProperty = (Collection<String>)params.get("pna");
        }
        /*---------------------2，解析set部分，并尝试填充版本字段*/
        MetaObject entityMetaObject = getConfiguration().newMetaObject(parameterObject);
        Map<String,Object> additionalMap = Maps.newHashMap();
        List<String> setPart = sqlSet(entityMetaObject, validProperty, additionalMap);


        String updateSql = String.format(
                UPDATE_SQL_TEMPLATE,
                tableInfo.getTableName(),
                MybatisSqlUtils.commaJoin(setPart),
                tableInfo.getKeyColumn(),
                tableInfo.getKeyProperty()
        );
        if(partial){
            additionalMap.put(tableInfo.getKeyProperty(), entityMetaObject.getValue(tableInfo.getKeyProperty()));
        }
        //谓语部分乐观锁处理
        if(tableInfo.getVersionField()!=null){
            TableFieldInfo versionField = tableInfo.getVersionField();
            updateSql += " AND "+versionField.getColumn()+"=#{"+versionField.getEl()+"}";
        }

        return SqlProviderBinding.of(updateSql,additionalMap);
    }

    private List<String> sqlSet(
            MetaObject entityMetaObject,
            Collection<String> partialProperty,
            Map<String,Object> additionalParam
    ) {
        List<String> sqlSetsPart = Lists.newArrayList();
        TableFieldInfo versionField = tableInfo.getVersionField();
        boolean versionResolved = false;

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();

            boolean shouldHandleProperty;
            if(partialProperty!=null){
                //如果业务特定列不为空，则以业务特定列是否包含这个属性为准，忽略DefaultUpdate配置
                shouldHandleProperty = partialProperty.contains(property);
            }else{
                //如果业务特定列为空，则以DefaultUpdate为准
                shouldHandleProperty = fieldInfo.isDefaultUpdate();
            }
            if(!shouldHandleProperty){
                continue;
            }

            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(property);

            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }

            boolean putSet = false;
            if(selective){
                if(useFieldInModifySql(fieldInfo,propertyType,propertyVal)){
                    putSet = true;
                }
            }else{
                putSet = true;
            }

            if(putSet){
                if(versionField!=null&&property.equals(versionField.getProperty())){
                    //如果插入列是乐观锁列
                    resolveVersionColumn(
                            entityMetaObject,
                            versionField,
                            sqlSetsPart,
                            additionalParam
                    );
                    versionResolved = true;
                }else{
                    sqlSetsPart.add(fieldInfo.getColumn()+"=#{"+fieldInfo.getEl()+"}");
                    if(partial){
                        //partial为true时，如果使用自动映射需要添加前缀，此处将数据提取，存入additionalParam
                        additionalParam.put(property,propertyVal);
                    }
                }
            }
        }

        if(versionField!=null&&!versionResolved){
            resolveVersionColumn(
                    entityMetaObject,
                    versionField,
                    sqlSetsPart,
                    additionalParam
            );
        }

        return sqlSetsPart;
    }

    //填充version相关set部分，并更新entity信息
    private void resolveVersionColumn(
            MetaObject entityMetaObject,
            TableFieldInfo versionField,
            List<String> sqlSetsPart,
            Map<String, Object> additionalParam
    ){
        //set version-->new_version
        String property = versionField.getProperty(),
               newVersionProperty = property+ MybatisSqlUtils.NEW_VERSION_STAMP_SUFFIX;
        Class<?> propertyType = entityMetaObject.getGetterType(property);
        Object propertyVal = entityMetaObject.getValue(property);

        if(propertyVal==null){
            throw new MybatisException("version is null,field:["+property+"]!");
        }else if(MybatisStringUtils.isCharSequence(propertyType)){
            if(MybatisStringUtils.isEmpty((String)propertyVal)){
                throw new MybatisException("version is null or empty,field:["+property+"]!");
            }
        }

        additionalParam.put(property,propertyVal);

        String newVersionStamp = IdWorker.getIdStr();
        sqlSetsPart.add(versionField.getColumn()+"=#{"+newVersionProperty+"}");
        additionalParam.put(newVersionProperty, newVersionStamp);

        entityMetaObject.setValue(property,newVersionStamp);
    }



    @Override
    public String getMethodId() {
        if(partial){
            return SqlMethod.UPDATE_PARTIAL_COLUMN_BY_ID.getMethod();
            //return "updatePartialColumnById";
        }else if(selective){
            return SqlMethod.UPDATE_BY_ID.getMethod();
            //return "updateById";
        }else{
            return SqlMethod.UPDATE_ALL_COLUMN_BY_ID.getMethod();
            //return "updateAllColumnById";
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UPDATE;
    }

}
