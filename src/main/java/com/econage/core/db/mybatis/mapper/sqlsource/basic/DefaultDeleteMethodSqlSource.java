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
package com.econage.core.db.mybatis.mapper.sqlsource.basic;

import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.sqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.sqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.Map;

/*
* 为删除方法提供sql解析
* */
public class DefaultDeleteMethodSqlSource extends AbstractDefaultMethodSqlSource {

    public static final String DELETE_BY_ID = "DELETE FROM %s WHERE %s=#{%s}",//"根据ID 删除一条数据"
            DELETE_BATCH_BY_IDS = "DELETE FROM %s WHERE %s IN (%s)";//"根据ID集合，批量删除数据"

    private final boolean batch;

    public DefaultDeleteMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo, boolean batch
    ) {
        super(configuration, tableInfo);
        this.batch = batch;
        this.deleteByIdSqlProviderBinding = SqlProviderBinding.of(String.format(
                DELETE_BY_ID,
                tableInfo.getTableName(),
                tableInfo.getKeyColumn(),
                tableInfo.getKeyProperty()
        ));
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(batch){
            return parseDeleteBatchByIds(parameterObject);
        }else{
            return parseDelete();
        }
    }

    @Override
    public String getMethodId() {
        if(batch){
            return SqlMethod.DELETE_BY_IDS.getMethod();
        }else{
            return SqlMethod.DELETE_BY_ID.getMethod();
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.DELETE;
    }

    private final SqlProviderBinding deleteByIdSqlProviderBinding;
    private SqlProviderBinding parseDelete(){
        return deleteByIdSqlProviderBinding;
    }

    private SqlProviderBinding parseDeleteBatchByIds(Object parameterObject){
        Collection<?> collectionParam =fetchCollectionTypeParameter(parameterObject);
        if(MybatisCollectionUtils.isEmpty(collectionParam)){
            //return SqlProviderBinding.of(" DELETE FROM " + tableInfo.getTableName() +" WHERE 1<>1 ");
            throw new IllegalArgumentException("parameter is empty!");
        }

        Map<String,Object> additionalParameter = Maps.newHashMap();
        String idTokens = MybatisSqlUtils.formatCollection2ParameterMappings(
                "","",tableInfo.getKeyProperty(),
                collectionParam,
                additionalParameter
        );
        String sql = String.format(
                DELETE_BATCH_BY_IDS,
                tableInfo.getTableName(), tableInfo.getKeyColumn(), idTokens
        );
        return SqlProviderBinding.of(sql,additionalParameter);
    }

}
