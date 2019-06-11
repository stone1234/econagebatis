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
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.Map;

public class DefaultSelectMethodSqlSource extends AbstractDefaultMethodSqlSource {

    public static final String SELECT_BY_ID = "SELECT %s FROM %s WHERE %s=#{%s}",
                                SELECT_LIST_BY_IDS = "SELECT %s FROM %s WHERE %s ",
                                SELECT_PAGE_LIST = "SELECT %s FROM %s";


    private final boolean batch;
    private final boolean page;

    public DefaultSelectMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo,
            boolean batch, boolean page
    ) {
        super(configuration, tableInfo);
        this.batch = batch;
        this.page = page;

        this.parsePageSelectSQLBinding = SqlProviderBinding.of(String.format(
                SELECT_PAGE_LIST,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName()
        ));
        this.parseSelectSQLBinding = SqlProviderBinding.of(String.format(
                SELECT_BY_ID,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName(),
                tableInfo.getKeyColumn(),
                tableInfo.getKeyProperty()
        ));
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(page&&batch){
            throw new IllegalStateException("page and batch both true!");
        }
        if(page){
            return parsePageSelect(parameterObject);
        }else if(batch){
            return parseSelectListByIds(parameterObject);
        }else{
            return parseSelect(parameterObject);
        }
    }

    @Override
    public String getMethodId() {
        if(page){
            return SqlMethod.SELECT_LIST_BY_PAGE.getMethod();
            //return "selectListByPage";
        }else if(batch){
            return SqlMethod.SELECT_LIST_BY_IDS.getMethod();
            //return "selectListByIds";
        }else{
            return SqlMethod.SELECT_BY_ID.getMethod();
            //return "selectById";
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }

    //提供基本全局查询逻辑即可，插件会自动侦测主键，并分页排序
    private final SqlProviderBinding parsePageSelectSQLBinding;
    private SqlProviderBinding parsePageSelect(Object parameterObject){
        /*String sql = String.format(
                SELECT_PAGE_LIST,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName()
        );
        return SqlProviderBinding.of(sql);*/
        return parsePageSelectSQLBinding;
    }

    private final SqlProviderBinding parseSelectSQLBinding;
    private SqlProviderBinding parseSelect(Object parameterObject){
        /*String sql = String.format(
                SELECT_BY_ID,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName(),
                tableInfo.getKeyColumn(),
                tableInfo.getKeyProperty()
        );
        return SqlProviderBinding.of(sql);*/
        return parseSelectSQLBinding;
    }


    private SqlProviderBinding parseSelectListByIds(Object parameterObject){
        Collection<?> collectionParam =fetchCollectionTypeParameter(parameterObject);
        if(MybatisCollectionUtils.isEmpty(collectionParam)){
            //如果参数为空，则返回一个查不出任何结果的sql
            return SqlProviderBinding.of(String.format(
                    SELECT_LIST_BY_IDS,
                    tableInfo.getSelectColumns(),
                    tableInfo.getTableName(),
                    MybatisSqlUtils.STATIC_FALSE_WHERE_SQL
            ));
        }

        Map<String,Object> additionalParameter = Maps.newHashMap();
        String idTokens = MybatisSqlUtils.formatCollection2ParameterMappings(tableInfo.getKeyProperty(), collectionParam, additionalParameter);
        String sql = String.format(
                SELECT_LIST_BY_IDS,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName(),
                tableInfo.getKeyColumn() + " IN ("+idTokens+")"
        );
        return SqlProviderBinding.of(sql,additionalParameter);
    }



}
