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

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.Map;

public class DefaultSelectByFkMethodSqlSource extends AbstractDefaultMethodSqlSource {

    public static final String SELECT_LIST_BY_FKS = "SELECT %s FROM %s WHERE %s ";

    //如果参数为空，则返回一个查不出任何结果的sql
    private final SqlProviderBinding emptyResultSQLBinding;
    public DefaultSelectByFkMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo
    ) {
        super(configuration, tableInfo);
        this.emptyResultSQLBinding = SqlProviderBinding.of(String.format(
                SELECT_LIST_BY_FKS,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName(),
                MybatisSqlUtils.STATIC_FALSE_WHERE_SQL
        ));
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        Collection<?> collectionParam =fetchCollectionTypeParameter(parameterObject);
        if(MybatisCollectionUtils.isEmpty(collectionParam)){
            return emptyResultSQLBinding;
        }else if(tableInfo.getFkField()==null){
            throw new MybatisException("Could not find fkField on table.Possibly no @TableFk in Entity.");
        }

        Map<String,Object> additionalParameter = Maps.newHashMap();
        String fkTokens = MybatisSqlUtils.formatCollection2ParameterMappings(
                tableInfo.getFkProperty(),
                collectionParam,
                additionalParameter
        );

        String sql = String.format(
                SELECT_LIST_BY_FKS,
                tableInfo.getSelectColumns(),
                tableInfo.getTableName(),
                tableInfo.getFkColumn() + " IN ("+fkTokens+")"
        );

        return SqlProviderBinding.of(sql,additionalParameter);
    }

    @Override
    public String getMethodId() {
        return SqlMethod.SELECT_LIST_BY_FK.getMethod();
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }

}
