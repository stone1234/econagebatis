package com.econage.core.db.mybatis.mapper.defaultsqlsource.bywhere;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.defaultsqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.wherelogic.MybatisWhereLogicHelper;

import java.util.List;
import java.util.Map;

public abstract class AbstractByWhereMethodSqlSource extends AbstractDefaultMethodSqlSource {
    public AbstractByWhereMethodSqlSource(MybatisConfiguration configuration, TableInfo tableInfo) {
        super(configuration, tableInfo);
    }

    /*
    * 如果谓语语句为空，则返回1=1
    * */
    protected String parseWhereLogicJoinSQL(Object parameterObject, Map<String,Object> additionalParameter){

        List<String> wherePart = MybatisWhereLogicHelper.parseWhereLogic(getGlobalAssistant(),parameterObject,additionalParameter);
        if(MybatisCollectionUtils.isEmpty(wherePart)){
            wherePart.add(MybatisSqlUtils.STATIC_TRUE_WHERE_SQL);
        }

        return MybatisSqlUtils.wherePartJoin(wherePart);
    }
}
