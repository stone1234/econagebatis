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

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import org.apache.ibatis.mapping.SqlCommandType;

public class DefaultSelectCountMethodSqlSource extends AbstractDefaultMethodSqlSource {

    private final String executeSQL;
    private final SqlProviderBinding sqlProviderBinding;

    public DefaultSelectCountMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo
    ) {
        super(configuration, tableInfo);
        this.executeSQL = "SELECT COUNT(1) FROM "+tableInfo.getTableName();
        this.sqlProviderBinding = SqlProviderBinding.of(this.executeSQL);
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        return sqlProviderBinding;
    }

    @Override
    public String getMethodId() {
        return SqlMethod.SELECT_COUNT_ALL.getMethod();
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }
}
