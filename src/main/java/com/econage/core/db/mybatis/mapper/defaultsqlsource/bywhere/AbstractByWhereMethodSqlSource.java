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
    * 控制是否填充false语句到空逻辑
    * */
    protected String parseWhereLogicJoinSQL(Object parameterObject, Map<String,Object> additionalParameter){

        List<String> wherePart = MybatisWhereLogicHelper.parseWhereLogic(getGlobalAssistant(),parameterObject,additionalParameter);
        //如果解析where部分后，没有任何条件，则执行空sql
        if(fillFalseSQL2EmptyWhereLogic()&& MybatisCollectionUtils.isEmpty(wherePart)){
            wherePart.add(MybatisSqlUtils.STATIC_FALSE_WHERE_SQL);
        }

        return MybatisSqlUtils.wherePartJoin(wherePart);
    }

    /*
    * 如果解析where逻辑是空的，是否填充静态false语句到逻辑中
    * */
    protected boolean fillFalseSQL2EmptyWhereLogic(){
        return true;
    }
}
