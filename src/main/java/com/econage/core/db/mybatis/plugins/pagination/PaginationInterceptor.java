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
package com.econage.core.db.mybatis.plugins.pagination;

import com.econage.core.db.mybatis.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.DBType;
import com.econage.core.db.mybatis.mapper.MapperConst;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/*
* type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
* 如果需要分页，此方法会被替换为
* @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
* */
//@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PaginationInterceptor implements Interceptor {

    private static final Log logger = LogFactory.getLog(PaginationInterceptor.class);

    private String dialectClazz;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        BoundSql boundSql;
        /*-------------------不需要分页的场合 */
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }
        if(rowBounds == null){
            //最大限度避免空值导致的异常，如果分页类型为空，则修改参数传入RowBounds.DEFAULT;
            args[2] = RowBounds.DEFAULT;
            return invocation.proceed();
        }else if ( rowBounds == RowBounds.DEFAULT ) {
            // 无需分页
            return invocation.proceed();
        }
        /*-------------------不需要分页的场合------------------- */
        /*-------------------准备分页上下文信息*/
        if(args.length == 4){
            //4 个参数时
            boundSql = mappedStatement.getBoundSql(parameter);
        } else {
            //6 个参数时
            boundSql = (BoundSql) args[5];
        }
        Pagination pagination;
        if(rowBounds instanceof Pagination){
            pagination = (Pagination)rowBounds;
        }else{
            pagination = Pagination.newPagination().rowBounds(rowBounds);
        }
        parsePaginationSortInfo(pagination,mappedStatement,parameter);
        PaginationContext paginationContext = new PaginationContext(
                boundSql.getSql(),
                pagination,
                getDbType(mappedStatement)
        );
        /*-------------------准备分页上下文信息-------------------*/

        String paginationSql = DialectFactory.buildPaginationSql(paginationContext,dialectClazz);
        List<ParameterMapping> parameterMappings = Lists.newArrayList();
        Map<String,Object> additionalParamMap = getBoundSqlAdditionalParameter(boundSql);
        //前置的参数映射
        if(paginationContext.getPaginationParamBefore()!=null){
            for(Object paramBefore : paginationContext.getPaginationParamBefore()){
                ParameterMapping parameterMapping = getPaginationParameterMappingByObject(mappedStatement.getConfiguration(), paramBefore);
                parameterMappings.add(parameterMapping);
                additionalParamMap.put(parameterMapping.getProperty(),paramBefore);
            }
        }
        //原有参数自带参数映射
        if(boundSql.getParameterMappings()!=null){
            parameterMappings.addAll(boundSql.getParameterMappings());
        }
        //后置的参数映射
        if(paginationContext.getPaginationParamAfter()!=null){
            for(Object paramBefore : paginationContext.getPaginationParamAfter() ){
                ParameterMapping parameterMapping = getPaginationParameterMappingByObject(mappedStatement.getConfiguration(), paramBefore);
                parameterMappings.add(parameterMapping);
                additionalParamMap.put(parameterMapping.getProperty(),paramBefore);
            }
        }

        BoundSql paginationBoundSql = new BoundSql(
                mappedStatement.getConfiguration(),
                paginationSql,
                parameterMappings,
                parameter
        );
        //填充参数
        for (Map.Entry<String,Object> entry : additionalParamMap.entrySet()) {
            paginationBoundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }

        CacheKey paginationKey = executor.createCacheKey(
                mappedStatement,
                parameter,
                pagination,
                paginationBoundSql
        );

		/*
         * <p> 禁用内存分页 </p>
		 */
        //执行分页查询
        return executor.query(
                mappedStatement,
                parameter,
                RowBounds.DEFAULT,
                resultHandler,
                paginationKey,
                paginationBoundSql
        );
    }


    private Field additionalParametersField;
    private Map<String,Object> getBoundSqlAdditionalParameter(BoundSql boundSql) throws IllegalAccessException {
        return (Map<String, Object>)additionalParametersField.get(boundSql);
    }


    private ParameterMapping getPaginationParameterMappingByObject(Configuration configuration, Object paramObject){
        //使用参数的hashcode作为属性，以保证不会出现属性名冲突
        ParameterMapping.Builder builder = new ParameterMapping.Builder(
                configuration,
                "pagination"+paramObject.hashCode(), paramObject.getClass()
        );
        return builder.build();
    }


    private TableInfo detectTableInfo(MappedStatement mappedStatement, Object parameter) throws ClassNotFoundException {
        TableInfo tableInfo = null;
        MybatisGlobalAssistant mybatisGlobalAssistant = ((MybatisConfiguration)mappedStatement.getConfiguration()).getGlobalAssistant();

        //尝试mapper
        String mappedStatementId = mappedStatement.getId();
        String mappedStatementNameSpace = mappedStatementId.substring(0,mappedStatementId.lastIndexOf("."));
        Class<?> mapperClass = Resources.classForName(mappedStatementNameSpace);
        tableInfo = mybatisGlobalAssistant.saveAndGetTableInfoByMapper(mapperClass);
        if(tableInfo!=null){
            return tableInfo;
        }
        //尝试返回类型
        if(mappedStatement.getResultMaps()!=null&&mappedStatement.getResultMaps().size()>0){
            Class<?> modelClass = mappedStatement.getResultMaps().get(0).getType();
            tableInfo = mybatisGlobalAssistant.saveAndGetTableInfoByModel(modelClass);
            if(tableInfo!=null){
                return tableInfo;
            }
        }
        //尝试参数类型
        /*if(parameter!=null){
            tableInfo = mybatisGlobalAssistant.saveAndGetTableInfoByModel(parameter.getClass());
            if(tableInfo!=null){
                return tableInfo;
            }
        }*/
        return null;
    }

    //如果排序为空，尝试填充表id列，
    //如果不为空，尝试将bean属性名转换为列名
    private void parsePaginationSortInfo(Pagination pagination, MappedStatement mappedStatement,Object parameter) throws ClassNotFoundException {
        //尝试查找内联类型的resultMap，提取里面的resultType
        //String inlineResultMapId = mappedStatement.getId()+"-Inline";
        //分页情况下，不应该使用多结果功能，此处取第一个resultMap的类型
        //Class<?> modelClass = mappedStatement.getResultMaps().get(0).getType();
        /*for(ResultMap defaultSelectResultMap : mappedStatement.getResultMaps()){
            if(inlineResultMapId.equals(defaultSelectResultMap.getId())){
                modelClass = defaultSelectResultMap.getType();
                break;
            }
        }*/
        if(parameter instanceof Map){
            Map<String, Object> params = (Map<String, Object>) parameter;
            if(params.containsKey(MapperConst.DYNA_CLASS_PARAM_NAME)){
                DynaClass dynaClazzObj = (DynaClass)params.get(MapperConst.DYNA_CLASS_PARAM_NAME);
                if(ArrayUtils.isEmpty(pagination.getSortName())){
                    pagination.setSortName( dynaClazzObj.getIdColumn() );
                }
                //如果是动态bean环境，则不再推测TableInfo
                return;
            }
        }

        parseByTableInfo(pagination,mappedStatement,parameter);
    }

    private void parseByTableInfo(Pagination pagination, MappedStatement mappedStatement,Object parameter) throws ClassNotFoundException {
        //尝试获取全局助手，并找到相关的表信息，Pagination存储的排序列是属性名，转换为列名
        TableInfo tableInfo = detectTableInfo(mappedStatement,parameter);
        if(tableInfo==null){
            //如果找不到，则不做转换
            return;
        }
        //尝试填充空白排序列的情况
        if(ArrayUtils.isEmpty(pagination.getSortName())){
            //如果未设置排序列，则以主键为准
            pagination.setSortName(tableInfo.getKeyColumn());
            return;
        }

        String[] sortPropertyArray = pagination.getSortName();
        String[] sortColumnArray = new String[sortPropertyArray.length];
        for( int i=0,l=sortPropertyArray.length;i<l;i++ ){
            String sorProperty = sortPropertyArray[i];
            String sortColumn = tableInfo.getAutoMappingColumnByProperty( sorProperty );
            //如果找不到属性对应的列名，则可能用了数据库列名，保留原数据
            if(MybatisStringUtils.isEmpty(sortColumn)){
                sortColumnArray[i] = sorProperty;
            }else{
                sortColumnArray[i] = sortColumn;
            }
        }
        pagination.setSortName(sortColumnArray);
    }


    private DBType getDbType(MappedStatement mappedStatement){
        MybatisConfiguration mybatisConfiguration = (MybatisConfiguration)mappedStatement.getConfiguration();
        return mybatisConfiguration.getGlobalAssistant().getDbType();
    }

    public PaginationInterceptor() {
        try {
            //反射获取 BoundSql 中的 additionalParameters 属性
            additionalParametersField = BoundSql.class.getDeclaredField(
                    "additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new MybatisException(e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties prop) {
        String dialectClazz = prop.getProperty("dialectClazz");
        if (MybatisStringUtils.isNotEmpty(dialectClazz)) {
            this.dialectClazz = dialectClazz;
        }
    }
    public PaginationInterceptor setDialectClazz(String dialectClazz) {
        this.dialectClazz = dialectClazz;
        return this;
    }
}
