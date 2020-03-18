package com.econage.core.db.mybatis.adaptation;

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.MapperConst;
import com.econage.core.db.mybatis.mapper.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.pagination.DialectFactory;
import com.econage.core.db.mybatis.pagination.Pagination;
import com.econage.core.db.mybatis.pagination.PaginationContext;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
* 执行器包装类，解决分页问题
* 后续考虑加入性能监控、缓存的功能
* */
public class MybatisExecutorMaster implements Executor {

    private static final Field additionalParametersField;
    static{
        //反射获取 BoundSql 中的 additionalParameters 属性
        try {
            additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new MybatisException(e.getMessage());
        }
    }

    private final Executor delegate;
    private final MybatisConfiguration configuration;
    private final MybatisGlobalAssistant mybatisGlobalAssistant;

    public MybatisExecutorMaster(Executor delegate, MybatisConfiguration configuration) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.mybatisGlobalAssistant = configuration.getGlobalAssistant();
        delegate.setExecutorWrapper(this);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        return query(ms, parameterObject, rowBounds, resultHandler,null, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey orgKey, BoundSql boundSql)
            throws SQLException {
        /*-------------------不需要分页*/
        /*未传入分页对象或者分页对象含义是获取全部*/
        if(rowBounds==null||rowBounds==RowBounds.DEFAULT){
            rowBounds = RowBounds.DEFAULT;
            CacheKey key = orgKey==null?createCacheKey(ms, parameterObject, rowBounds, boundSql):orgKey;
            return delegate.query(
                    ms,
                    parameterObject,
                    rowBounds,
                    resultHandler,
                    key,
                    boundSql
            );
        }
        /*-------------------不需要分页-------------------*/

        return doPaginationQuery(ms, parameterObject, rowBounds, resultHandler, boundSql);
    }

    private <E> List<E> doPaginationQuery(
            final MappedStatement mappedStatement,
            final Object parameter,
            final RowBounds rowBounds,
            final ResultHandler resultHandler,
            final BoundSql boundSql
    ) throws SQLException {
        try{

            /*-------------------准备分页上下文信息*/
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
                    mybatisGlobalAssistant.getDbType()
            );
            /*-------------------准备分页上下文信息-------------------*/

            /*-------------------提取分页解析逻辑可能用到的额外参数*/
            String paginationSql = DialectFactory.buildPaginationSql(paginationContext);
            List<ParameterMapping> parameterMappings = new ArrayList<>();
            Map<String,Object> additionalParamMap = getBoundSqlAdditionalParameter(boundSql);
            //前置的参数映射
            if(paginationContext.getPaginationParamBefore()!=null){
                for(Object paramBefore : paginationContext.getPaginationParamBefore()){
                    ParameterMapping parameterMapping = newPaginationParameterMappingByObject( paramBefore);
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
                    ParameterMapping parameterMapping = newPaginationParameterMappingByObject( paramBefore);
                    parameterMappings.add(parameterMapping);
                    additionalParamMap.put(parameterMapping.getProperty(),paramBefore);
                }
            }
            /*-------------------提取分页解析逻辑可能用到的额外参数-------------------*/
            /*-------------------组装分页语句对应的组件*/
            BoundSql paginationBoundSql = new BoundSql(
                    configuration,
                    paginationSql,
                    parameterMappings,
                    parameter
            );
            //填充参数
            for (Map.Entry<String,Object> entry : additionalParamMap.entrySet()) {
                paginationBoundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
            }

            CacheKey paginationKey = createCacheKey(
                    mappedStatement,
                    parameter,
                    pagination,
                    paginationBoundSql
            );
            /*-------------------组装分页语句对应的组件-------------------*/

            return delegate.query(
                    mappedStatement,
                    parameter,
                    RowBounds.DEFAULT,
                    resultHandler,
                    paginationKey,
                    paginationBoundSql
            );

        }catch(Exception e){
            throw new SQLException(e.getMessage(),e);
        }
    }

    private Map<String,Object> getBoundSqlAdditionalParameter(BoundSql boundSql) throws IllegalAccessException {
        return (Map<String, Object>)additionalParametersField.get(boundSql);
    }

    private ParameterMapping newPaginationParameterMappingByObject(Object paramObject){
        //使用参数的hashcode作为属性，以保证不会出现属性名冲突
        ParameterMapping.Builder builder = new ParameterMapping.Builder(
                configuration,
                "pagination"+paramObject.hashCode(), paramObject.getClass()
        );
        return builder.build();
    }

    private TableInfo detectTableInfo(MappedStatement mappedStatement, Object parameter) throws ClassNotFoundException {
        TableInfo tableInfo;

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
        return null;
    }

    //如果排序为空，尝试填充表id列，
    //如果不为空，尝试将bean属性名转换为列名
    private void parsePaginationSortInfo(Pagination pagination, MappedStatement mappedStatement,Object parameter) throws ClassNotFoundException {
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

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        delegate.close(forceRollback);
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        return delegate.update(ms, parameterObject);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        return delegate.queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        delegate.rollback(required);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

}
