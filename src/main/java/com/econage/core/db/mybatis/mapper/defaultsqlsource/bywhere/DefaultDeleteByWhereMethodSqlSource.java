package com.econage.core.db.mybatis.mapper.defaultsqlsource.bywhere;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Map;

public class DefaultDeleteByWhereMethodSqlSource extends AbstractByWhereMethodSqlSource {
    public static final String DELETE_BY_WHERE_LOGIC_TPL = "DELETE FROM %s WHERE %s";

    //如果参数为空，则返回一个不删除任何结果的sql
    private final SqlProviderBinding emptyResultSQLBinding;
    public DefaultDeleteByWhereMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo
    ) {
        super(configuration, tableInfo);
        this.emptyResultSQLBinding = SqlProviderBinding.of(String.format(
                DELETE_BY_WHERE_LOGIC_TPL,
                tableInfo.getTableName(),
                MybatisSqlUtils.STATIC_FALSE_WHERE_SQL
        ));
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(parameterObject==null){
            return emptyResultSQLBinding;
        }

        Map<String,Object> additionalParameter = Maps.newHashMap();
        String sql = String.format(
                DELETE_BY_WHERE_LOGIC_TPL,
                tableInfo.getTableName(),
                parseWhereLogicJoinSQL(parameterObject,additionalParameter)
        );

        return SqlProviderBinding.of(sql,additionalParameter);
    }

    @Override
    public String getMethodId() {
        return SqlMethod.DELETE_BY_WHERE_LOGIC.getMethod();
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.DELETE;
    }
}
