package com.econage.core.db.mybatis.mapper.base;

import com.econage.core.db.mybatis.entity.TableFieldInfo;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.FieldStrategy;
import com.econage.core.db.mybatis.mapper.provider.MybatisProviderContext;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.google.common.base.Strings;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class SqlProviderHelper {
    public static final String STATIC_WHERE_SQL_FRAGMENT = " where ";
    public static final String STATIC_FALSE_WHERE = STATIC_WHERE_SQL_FRAGMENT+ MybatisSqlUtils.STATIC_FALSE_WHERE_SQL;
    public static final String STATIC_TRUE_WHERE = STATIC_WHERE_SQL_FRAGMENT+ MybatisSqlUtils.STATIC_TRUE_WHERE_SQL;

    /*
    * whereLogic方式解析，由两部分组成，一部分是sql基础部分，由select和from组成
    * 此函数负责wherelogic解析及统一处理，crud通用
    * 如果wherelogic为空，则返回结果一直为空
    * */
    public static String parseWhereLogic(MybatisProviderContext context, Object whereLogic){
        if(whereLogic==null){
            return SqlProviderHelper.STATIC_FALSE_WHERE;
        }

        List<String> wherePart = context.parseWhereLogic(whereLogic);
        //有whereLogic对象，但没有谓语解析逻辑，则直接显示全部
        if(MybatisCollectionUtils.isEmpty(wherePart)){
            return MybatisStringUtils.EMPTY;
        }

        return MybatisSqlUtils.wherePartJoin(STATIC_WHERE_SQL_FRAGMENT,wherePart);
    }

    /*
    * 处理id集合where部分逻辑
    * 简单删除，查询方法用到
    * */
    public static String parseIdCollectionWherePart(
            MybatisProviderContext context,
            @Param("collection") Collection<? extends Serializable> idList
    ){
        TableInfo tableInfo = context.getTableInfo();
        return " where " +  tableInfo.getKeyColumn()+
                " in ( " + context.formatCollection2ParameterMappings(tableInfo.getKeyProperty(),idList)+ " )";
    }

    //某个字段在默认的修改、插入操作中，是否可以被使用，依据是否为空策略
    public static boolean useFieldInModifySql(TableFieldInfo fieldInfo, Class<?> propertyType, Object propertyVal){
        boolean canUse = false;
        if(FieldStrategy.IGNORED==fieldInfo.getFieldStrategy()){
            canUse = true;
        }else if(FieldStrategy.NOT_NULL==fieldInfo.getFieldStrategy()){
            canUse = propertyVal!=null;
        }else if(FieldStrategy.NOT_EMPTY==fieldInfo.getFieldStrategy()){
            if(MybatisStringUtils.isCharSequence(propertyType)){
                canUse = !Strings.isNullOrEmpty((String)propertyVal);
            }else{
                canUse = propertyVal!=null;
            }
        }
        return canUse;
    }
}
