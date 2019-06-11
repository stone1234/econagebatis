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

import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.IdType;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.econage.core.db.mybatis.uid.dbincrementer.IKeyGenerator;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableFieldInfo;
import com.econage.core.db.mybatis.uuid.IdWorker;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.List;

public class DefaultInsertMethodSqlSource extends AbstractDefaultMethodSqlSource {
    public static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s)";


    private final boolean selective;
    /*
     * 插入方法，在mybatis注册时需要提供key相关的信息
     * 由DefaultInsertMethodSqlSource根据table信息来决定提供的信息
     * keyGenerator为空，则DefaultInsertMethodSqlSource负责填充key值，或者由业务程序提前填充
     * keyGenerator不为空，则认为由mybatis框架负责填充key值
     * */
    private KeyGenerator keyGenerator;
    private boolean saveAndGetKeyGeneratorRun;

    public DefaultInsertMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo, boolean selective
    ) {
        super(configuration, tableInfo);
        this.selective = selective;
    }

    /*-------------------插入时解析自增主键及sequence，对于oracle，默认使用sys_guid()函数 */
    public KeyGenerator saveAndGetKeyGenerator(MapperBuilderAssistant mapperBuilder){
        if(keyGenerator==null){
            /*
            * 仅在通过sequence或者数据库自增时，才需要为mybatis提供主键信息
             * 判断，是否需要在解析时，注入sql
            * true:插入前注入id值
            * false:由mybatis处理id值
            * */
            if (MybatisStringUtils.isNotEmpty(tableInfo.getKeyProperty())) {
                if (tableInfo.getIdType() == IdType.AUTO) {
                    /* 自增主键,mysql、sql server可用 */
                    keyGenerator = new Jdbc3KeyGenerator();
                }else if(null != tableInfo.getKeySequence()){
                    keyGenerator = genKeyGenerator(mapperBuilder);
                }
            }
        }
        saveAndGetKeyGeneratorRun = true;
        return keyGenerator;
    }

    private KeyGenerator genKeyGenerator(
            MapperBuilderAssistant builderAssistant
    ) {
        IKeyGenerator keyGenerator = getConfiguration().getGlobalAssistant().getKeyGenerator();
        if (null == keyGenerator) {
            throw new IllegalArgumentException("not configure IKeyGenerator implementation class.");
        }
        String selectKeyGeneratorId = getMethodId() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Class<?> resultTypeClass = tableInfo.getKeySequence().clazz();
        String keyProperty = tableInfo.getKeyProperty();
        String keyColumn = tableInfo.getKeyColumn();
        SqlSource sqlSource = createStaticSqlSource(
                keyGenerator.executeSql(tableInfo.getKeySequence().value()),
                resultTypeClass
        );
        builderAssistant.addMappedStatement(
                selectKeyGeneratorId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                null, null,
                null, null,
                null, resultTypeClass, null,
                false, false, false,
                NoKeyGenerator.INSTANCE, keyProperty, keyColumn,
                null,
                getConfiguration().getLanguageDriver(null),
                null
        );
        selectKeyGeneratorId = builderAssistant.applyCurrentNamespace(selectKeyGeneratorId, false);
        MappedStatement keyStatement = builderAssistant.getConfiguration().getMappedStatement(selectKeyGeneratorId, false);
        SelectKeyGenerator selectKeyGenerator = new SelectKeyGenerator(keyStatement, true);
        builderAssistant.getConfiguration().addKeyGenerator(selectKeyGeneratorId, selectKeyGenerator);
        return selectKeyGenerator;
    }
    /*-------------------插入时解析自增主键及sequence，对于oracle，默认使用sys_guid()函数-------------------*/

    @Override
    protected SqlProviderBinding parseBinding(Object entityObject) {
        //参数及状态检查
        Preconditions.checkNotNull(entityObject,"can not insert null object!");

        //变量准备
        MetaObject entityMetaObject = getConfiguration().newMetaObject(entityObject);
        List<String> columns = Lists.newArrayList();
        List<String> parameterTokens = Lists.newArrayList();

        if(!saveAndGetKeyGeneratorRun){
            throw new IllegalStateException("not try save KeyGenerator!");
        }
        TableFieldInfo versionField = tableInfo.getVersionField();
        if(versionField!=null){
            String property = versionField.getProperty();
            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(versionField.getProperty());
            if(propertyVal==null){
                throw new MybatisException("version is null,field:["+property+"]!");
            }else if(MybatisStringUtils.isCharSequence(propertyType)){
                if(MybatisStringUtils.isEmpty((String)propertyVal)){
                    throw new MybatisException("version is null or empty,field:["+property+"]!");
                }
            }
        }

        parseId(entityMetaObject,columns,parameterTokens);

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();
            //如果字段在默认的插入方法中无效
            if(!fieldInfo.isDefaultInsert()){
                continue;
            }

            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(property);
            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }

            if(selective){
                if(useFieldInModifySql(fieldInfo,propertyType,propertyVal)){
                    columns.add(fieldInfo.getColumn());
                    parameterTokens.add("#{"+fieldInfo.getEl()+"}");
                }
            }else{
                columns.add(fieldInfo.getColumn());
                parameterTokens.add("#{"+fieldInfo.getEl()+"}");
            }

        }
        /*public static final String INSERT_SQL_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s)";*/
        String sql = String.format(
                INSERT_SQL_TEMPLATE,
                tableInfo.getTableName(),
                MybatisSqlUtils.commaJoin(columns),
                MybatisSqlUtils.commaJoin(parameterTokens)
        );

        return SqlProviderBinding.of(sql);
    }

    private void parseId(
            MetaObject entityMetaObject,
            List<String> columns,
            List<String> parameterTokens
    ){
        if (MybatisStringUtils.isEmpty(tableInfo.getKeyProperty())) {
            return;
        }else if (tableInfo.getIdType() == IdType.AUTO||tableInfo.getIdType()==null) {
            /* 自增主键 不做任何操作，生成的插入语句也不需要描述列信息,mysql,ms sql server场景 */
            return;
        }

        if (null != tableInfo.getKeySequence()) {
            //通过sequence等数据库查询方式获取的主键，由mybatis框架负责注入id值
        } else {
            //其他uuid方式，则在此处注入主键，如果主键属性已经有值，则不注入
            Object idValue = entityMetaObject.getValue(tableInfo.getKeyProperty());
            if (MybatisStringUtils.checkValNull(idValue)) {
                if (tableInfo.getIdType() == IdType.ID_WORKER) {
                    entityMetaObject.setValue(tableInfo.getKeyProperty(), IdWorker.getIdStr());
                } else if (tableInfo.getIdType() == IdType.UUID) {
                    entityMetaObject.setValue(tableInfo.getKeyProperty(), IdWorker.get32UUID());
                }
            }
        }
        columns.add(tableInfo.getKeyColumn());
        parameterTokens.add("#{"+ tableInfo.getKeyProperty()+"}");
    }


    @Override
    public String getMethodId() {
        if(selective){
            return SqlMethod.INSERT.getMethod();
        }else{
            return SqlMethod.INSERT_ALL_COLUMN.getMethod();
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }


}
