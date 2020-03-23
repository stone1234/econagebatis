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
package com.flowyun.cornerstone.db.mybatis.adaptation;

import com.flowyun.cornerstone.db.mybatis.entity.MybatisTableInfoHelper;
import com.flowyun.cornerstone.db.mybatis.entity.TableInfo;
import com.flowyun.cornerstone.db.mybatis.enums.DBType;
import com.flowyun.cornerstone.db.mybatis.enums.FieldStrategy;
import com.flowyun.cornerstone.db.mybatis.enums.IdType;
import com.flowyun.cornerstone.db.mybatis.uid.dbincrementer.IKeyGenerator;
import com.flowyun.cornerstone.db.mybatis.util.MybatisClassUtils;
import com.flowyun.cornerstone.db.mybatis.util.MybatisJdbcUtils;
import com.flowyun.cornerstone.db.mybatis.util.MybatisPreconditions;
import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;
import com.flowyun.cornerstone.db.mybatis.wherelogic.MybatisWhereLogicHelper;
import com.flowyun.cornerstone.db.mybatis.wherelogic.WhereLogicInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <p>
 * Mybatis全局助手，维护额外的配置信息，提供若干辅助方法
 * </p>
 */
public class MybatisGlobalAssistant implements Serializable {
    private static final Log LOGGER = LogFactory.getLog(MybatisGlobalAssistant.class);

    //配置类，与助手类互相引用
    private final MybatisConfiguration configuration;

    // 数据库类型
    private DBType dbType;
    // 主键类型（默认 ID_WORKER）
    private IdType defaultIdType = IdType.ID_WORKER;
    // 列名称通用格式前缀
    private String dbColumnPrefix = MybatisStringUtils.EMPTY;
    // 列名称通用格式后缀
    private String dbColumnSuffix = MybatisStringUtils.UNDERLINE_STR;
    // 是否大写命名
    private boolean isCapitalMode = false;

    // 表关键词 key 生成器
    private IKeyGenerator keyGenerator;
    // 字段验证策略
    private FieldStrategy defaultFieldStrategy = FieldStrategy.NOT_NULL;
    // 标识符
    private String identifierQuote;
    // 缓存已注入CRUD的Mapper类信息
    private Set<String> mapperRegistryCache = new ConcurrentSkipListSet<>();
    //两个角度维护表信息，一个通过mapper类，一个通过model类
    private final ConcurrentHashMap<Class<?>, TableInfo> mapperTableInfoMap = new ConcurrentHashMap<>(5000);
    private final ConcurrentHashMap<Class<?>, TableInfo> modelTableInfoMap = new ConcurrentHashMap<>(5000);
    /*已经解析过的搜索表单类，会存在whereLogicInfoMap和excludeWhereLogicType中*/
    private final ConcurrentHashMap<Class<?>, WhereLogicInfo> whereLogicInfoMap = new ConcurrentHashMap<>(5000);
    private final Set<String> excludeWhereLogicType = new ConcurrentSkipListSet<>();

    private boolean globalCacheEnabled;

    private Set<String> disabledPropertyInDefaultUpdateMethod;
    {
        disabledPropertyInDefaultUpdateMethod = new HashSet<>();
        disabledPropertyInDefaultUpdateMethod.add("createDate");
        disabledPropertyInDefaultUpdateMethod.add("createUser");
    }

    public MybatisGlobalAssistant(MybatisConfiguration configuration) {
        this.configuration = configuration;
    }


    /*----------------------GETTER AND SETTER */
    public IKeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(IKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public DBType getDbType() {
        return dbType;
    }

    /**
     * 根据jdbcUrl设置数据库类型
     *
     * @param jdbcUrl
     */
    public void setDbTypeOfJdbcUrl(String jdbcUrl) {
        this.dbType = MybatisJdbcUtils.getDbType(jdbcUrl);
    }

    public void setDbType(String dbType) {
        this.dbType = DBType.getDBType(dbType);
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public IdType getDefaultIdType() {
        return defaultIdType;
    }

    public void setDefaultIdType(String defaultIdType) {
        this.defaultIdType = IdType.valueOf(defaultIdType);
    }

    public boolean isDbColumnUnderline() {
        return configuration.isMapUnderscoreToCamelCase();
    }

    /*public void setSqlInjector(SqlInjector sqlInjector) {
        this.sqlInjector = sqlInjector;
    }*/

    public FieldStrategy getDefaultFieldStrategy() {
        return defaultFieldStrategy;
    }

    public void setDefaultFieldStrategy(int defaultFieldStrategy) {
        this.defaultFieldStrategy = FieldStrategy.getFieldStrategy(defaultFieldStrategy);
    }

    public boolean isCapitalMode() {
        return isCapitalMode;
    }

    public void setCapitalMode(boolean isCapitalMode) {
        this.isCapitalMode = isCapitalMode;
    }

    public String getDbColumnPrefix() {
        return dbColumnPrefix;
    }

    public void setDbColumnPrefix(String dbColumnPrefix) {
        this.dbColumnPrefix = dbColumnPrefix;
    }

    public String getDbColumnSuffix() {
        return dbColumnSuffix;
    }

    public void setDbColumnSuffix(String dbColumnSuffix) {
        this.dbColumnSuffix = dbColumnSuffix;
    }

    public MybatisConfiguration getConfiguration() {
        return configuration;
    }

    public String getIdentifierQuote() {
        if (null == identifierQuote) {
            return dbType.getQuote();
        }
        return identifierQuote;
    }
    public void setIdentifierQuote(String identifierQuote) {
        this.identifierQuote = identifierQuote;
    }

    public boolean isGlobalCacheEnabled() {
        return globalCacheEnabled;
    }

    public void setGlobalCacheEnabled(boolean globalCacheEnabled) {
        this.globalCacheEnabled = globalCacheEnabled;
    }

    /*
    * todo 分布式缓存方案
    * */
    /*private MybatisCacheAssistant mybatisCacheAssistant;

    public MybatisCacheAssistant getMybatisCacheAssistant() {
        return mybatisCacheAssistant;
    }

    public void setMybatisCacheAssistant(MybatisCacheAssistant mybatisCacheAssistant) {
        this.mybatisCacheAssistant = mybatisCacheAssistant;
    }*/
    /*----------------------GETTER AND SETTER----------- */

    public boolean isMapperParsed(Class<?> mapper){
        if(!isMapperClass(mapper)){
            throw new IllegalArgumentException("it's not a mapper class.");
        }
        return mapperRegistryCache.contains(mapper.getName());
    }

    //仅处理接口，如果继承了BaseMapper或者有Mapper注解，则认为是一个mapper对象
    public boolean isMapperClass(Class<?> resolvedCls){
        if(resolvedCls!=null&&resolvedCls.isInterface()&&!MybatisClassUtils.isTopMapperInterface(resolvedCls)){
            if(MybatisClassUtils.isAssignableFromTopMapperInterface(resolvedCls)){
                return true;
            }else if(resolvedCls.getAnnotation(Mapper.class)!=null){
                return true;
            }
        }
        return false;
    }

    public TableInfo saveAndGetTableInfoByMapper(Class<?> mapperClass) {
        if(!isMapperClass(mapperClass)){
            return null;
        }
        if(mapperTableInfoMap.containsKey(mapperClass)){
            return mapperTableInfoMap.get(mapperClass);
        }
        Class<?> modelClass = MybatisClassUtils.extractModelClass(mapperClass);
        if(modelClass==null){
            return null;
        }
        TableInfo tableInfo = saveAndGetTableInfoByModel(modelClass);
        if(tableInfo!=null){
            mapperTableInfoMap.putIfAbsent(mapperClass,tableInfo);
            mapperRegistryCache.add(mapperClass.getName());
            return mapperTableInfoMap.get(mapperClass);
        }
        return null;
    }

    public TableInfo saveAndGetTableInfoByModel(Class<?> modelClass){
        if(modelClass==null||modelClass.isInterface()){
            return null;
        }

        if(modelTableInfoMap.containsKey(modelClass)){
            return modelTableInfoMap.get(modelClass);
        }
        TableInfo tableInfo = MybatisTableInfoHelper.parseTableInfo(this,modelClass);
        modelTableInfoMap.putIfAbsent(modelClass,tableInfo);
        return modelTableInfoMap.get(modelClass);
    }

    public WhereLogicInfo saveAndGetWhereLogic(Class<?> whereLogicCls){
        if(whereLogicCls==null){
            return null;
        }
        WhereLogicInfo whereLogicInfo = whereLogicInfoMap.get(whereLogicCls);
        if(whereLogicInfo !=null){
            //如果是已经成功解析过的搜索类，则直接返回
            return whereLogicInfo;
        }else if(excludeWhereLogicType.contains(whereLogicCls.getName())){
            //如果已经解析过的搜索类，不是搜索表单，则直接返回空
            return null;
        }

        synchronized (whereLogicCls){
            whereLogicInfo = whereLogicInfoMap.get(whereLogicCls);
            if(whereLogicInfo !=null){
                //如果是已经成功解析过的搜索类，则直接返回
                return whereLogicInfo;
            }else if(excludeWhereLogicType.contains(whereLogicCls.getName())){
                //如果已经解析过的搜索类，不是搜索表单，则直接返回空
                return null;
            }

            whereLogicInfo = MybatisWhereLogicHelper.parseWhereLogicInfo(this,whereLogicCls);
            if(whereLogicInfo ==null){
                excludeWhereLogicType.add(whereLogicCls.getName());
                return null;
            }
            whereLogicInfoMap.put(whereLogicCls, whereLogicInfo);
        }

        return whereLogicInfoMap.get(whereLogicCls);
    }

    //格式化没有手动备注的列名表名
    public String formatColumn(String property){
        MybatisPreconditions.checkNotNull(property,"column is null!");
        if(isDbColumnUnderline()){
            property = MybatisStringUtils.camelToUnderline(dbColumnPrefix,property,dbColumnSuffix);
        }
        if(isCapitalMode){
            property = property.toUpperCase();
        }
        return property;
    }

    public String formatTableName(String tableName){
        return formatColumn(tableName);
    }

    public boolean enableInDefaultUpdateMethod(String propertyName){
        if(disabledPropertyInDefaultUpdateMethod.contains(propertyName)){
            return false;
        }
        return true;
    }

    public void addDisabledPropertyInDefaultUpdateMethod(Collection<String> properties){
        if(properties!=null){
            disabledPropertyInDefaultUpdateMethod.addAll(properties);
        }
    }

    public boolean isValidModel(Class<?> modelClass){
        return modelTableInfoMap.contains(modelClass);
    }
}
