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
package com.econage.core.db.mybatis.adaptation;

import com.econage.core.db.mybatis.dyna.adaptation.DynaBeanExecutor;
import com.econage.core.db.mybatis.dyna.adaptation.DynaBeanResultSetHandler;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;


//拦截mapper注册器
public class MybatisConfiguration extends Configuration {
    private static final Log logger = LogFactory.getLog(MybatisConfiguration.class);

    /*
     * Mapper 注册
     * todo
     */
    public final MybatisMapperRegistry mybatisMapperRegistry = new MybatisMapperRegistry(this);

    /*
     * 构建助手类,todo
     * */
    private MybatisGlobalAssistant globalAssistant;

    /**
     * 初始化调用
     */
    public MybatisConfiguration() {
        if(logger.isDebugEnabled()){
            logger.debug("Mybatis init success.");
        }
    }

    @Override
    public void addMappedStatement(MappedStatement ms) {
        if(logger.isDebugEnabled()){
            logger.debug("addMappedStatement: " + ms.getId());
        }
        if (this.mappedStatements.containsKey(ms.getId())) {
            /*已加载了xml中的节点； 忽略mapper中的SqlProvider数据*/
            logger.error("mapper[" + ms.getId() + "] is ignored, because it's exists, maybe from xml file");
            return;
        }
        super.addMappedStatement(ms);
    }

    @Override
    public MapperRegistry getMapperRegistry() {
        return mybatisMapperRegistry;
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        mybatisMapperRegistry.addMapper(type);
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        mybatisMapperRegistry.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        mybatisMapperRegistry.addMappers(packageName);
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mybatisMapperRegistry.getMapper(type, sqlSession);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return mybatisMapperRegistry.hasMapper(type);
    }

    public MybatisGlobalAssistant getGlobalAssistant() {
        return globalAssistant;
    }

    public void setGlobalAssistant(MybatisGlobalAssistant globalAssistant) {
        this.globalAssistant = globalAssistant;
    }

    /*
    todo 增加二级缓存,暂时禁用所有二级缓存
    if (globalAssistant.getMybatisCacheAssistant()!=null) {
            executor = new MybatisCachingExecutor(executor);
    }*/
    @Override
    public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        Executor executor;
        if (ExecutorType.BATCH == executorType) {
            executor = new BatchExecutor(this, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            executor = new ReuseExecutor(this, transaction);
        } else {
            executor = new SimpleExecutor(this, transaction);
        }

        if(globalAssistant.isDynaBeanEnabled()){
            executor = new DynaBeanExecutor(globalAssistant, executor);
        }

        /*if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }*/
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }

    @Override
    public ResultSetHandler newResultSetHandler(
            Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
            ResultHandler resultHandler, BoundSql boundSql
    ){
        if(globalAssistant.isDynaBeanEnabled()){
            ResultSetHandler resultSetHandler = new DynaBeanResultSetHandler(
                    globalAssistant,
                    executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds
            );
            resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
            return resultSetHandler;
        }else{
            return super.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
        }
    }

}
