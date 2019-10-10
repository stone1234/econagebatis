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
package com.econage.core.db.mybatis.mapper.sqlsource.bywhere;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.sqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Map;

public class DefaultSelectByWhereMethodSqlSource extends AbstractByWhereMethodSqlSource {

    public static final String SELECT_LIST_BY_SEARCH_FORM = "SELECT %s FROM %s WHERE %s ";

    private final SqlProviderBinding emptyResultSQLBinding;
    private final boolean selectCount;
    public DefaultSelectByWhereMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo,
            boolean selectCount
    ) {
        super(configuration, tableInfo);
        this.emptyResultSQLBinding = SqlProviderBinding.of(String.format(
                SELECT_LIST_BY_SEARCH_FORM,
                parseSelectPart(),
                this.tableInfo.getTableName(),
                MybatisSqlUtils.STATIC_FALSE_WHERE_SQL
        ));
        this.selectCount = selectCount;
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(parameterObject==null){
            return emptyResultSQLBinding;
        }

        Map<String,Object> additionalParameter = Maps.newHashMap();
        String sql = String.format(
                SELECT_LIST_BY_SEARCH_FORM,
                parseSelectPart(),
                tableInfo.getTableName(),
                parseWhereLogicJoinSQL(parameterObject,additionalParameter)
        );

        return SqlProviderBinding.of(sql,additionalParameter);
    }

    private String parseSelectPart(){
        return selectCount?"COUNT(1)": tableInfo.getSelectColumns();
    }

    @Override
    public String getMethodId() {
        return selectCount?
                SqlMethod.SELECT_COUNT_BY_WHERE_LOGIC.getMethod():
                SqlMethod.SELECT_LIST_BY_WHERE_LOGIC.getMethod();
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }

}
