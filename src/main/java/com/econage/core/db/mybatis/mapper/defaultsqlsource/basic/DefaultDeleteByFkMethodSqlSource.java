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

/*
* 为删除方法提供sql解析
* */
public class DefaultDeleteByFkMethodSqlSource extends AbstractDefaultMethodSqlSource {

    public static final String DELETE_BY_FK = "DELETE FROM %s WHERE %s=#{%s}";//"根据ID集合，批量删除数据"

    private final SqlProviderBinding deleteByFkProvider;
    public DefaultDeleteByFkMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo
    ) {
        super(configuration, tableInfo);
        deleteByFkProvider = SqlProviderBinding.of(String.format(
                DELETE_BY_FK,
                tableInfo.getTableName(),
                tableInfo.getFkColumn(),
                tableInfo.getFkProperty()
        ));
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        return deleteByFkProvider;
    }

    @Override
    public String getMethodId() {
        return SqlMethod.DELETE_BY_FK.getMethod();
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.DELETE;
    }

}
