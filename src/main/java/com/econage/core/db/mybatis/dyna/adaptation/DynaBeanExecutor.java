package com.econage.core.db.mybatis.dyna.adaptation;

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.econage.core.db.mybatis.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.mapper.MapperConst;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DynaBeanExecutor implements Executor {

    private final MybatisGlobalAssistant globalAssistant;
    //名称不可更改
    private final Executor delegate;
    protected Executor wrapper;
    //是否执行在环境中使用了DynaClass
    //简单结果集可以不用volatile修饰，将来如果支持懒加载，则添加volatile修饰
    private boolean isRunningWithDynaClass;

    public DynaBeanExecutor(MybatisGlobalAssistant globalAssistant, Executor delegate) {
        if(!(delegate instanceof BaseExecutor)){
            throw new MybatisException("not a BaseExecutor,maybe a code error in system");
        }

        this.globalAssistant = globalAssistant;
        this.delegate = delegate;
        this.wrapper = this;
        delegate.setExecutorWrapper(this);
    }

    public Executor getDelegate() {
        return delegate;
    }

    //其他自定义mapper，只要传入的参数中带了DynaClass，就识别
    private void detectIsRunningWithDynaClass(MappedStatement ms, Object parameter){
        if(isRunningWithDynaClass){
            return;
        }

        if(parameter instanceof Map){
            Map<String, Object> params = (Map<String, Object>) parameter;
            Object dynaClazzObj = params.get(MapperConst.DYNA_CLASS_PARAM_NAME);
            if(dynaClazzObj instanceof DynaClass){
                isRunningWithDynaClass = true;
                globalAssistant.putExecutorDynaCls(delegate,(DynaClass)dynaClazzObj);
            }
        }
    }
    private void closeHook(){
        if(isRunningWithDynaClass){
            globalAssistant.removeExecutorDynaCls(delegate);
            isRunningWithDynaClass = false;
        }
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        detectIsRunningWithDynaClass(ms,parameter);
        return delegate.update(ms,parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        detectIsRunningWithDynaClass(ms, parameter);
        return delegate.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        detectIsRunningWithDynaClass(ms, parameter);
        return delegate.query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        detectIsRunningWithDynaClass(ms, parameter);
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
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad( ms, resultObject, property, key,targetType);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        try{
            delegate.close(forceRollback);
        }finally {
            closeHook();
        }
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        this.wrapper = executor;
    }
}
