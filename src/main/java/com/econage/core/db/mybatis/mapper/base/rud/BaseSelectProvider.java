package com.econage.core.db.mybatis.mapper.base.rud;

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.provider.MybatisProviderContext;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

import java.io.Serializable;
import java.util.Collection;

import static com.econage.core.db.mybatis.mapper.MapperConst.WHERE_LOGIC_PARAM_NAME;

/*
* SqlProvider已做增强
* 不再使用MethodSqlSource，交由Mybatis解析sqlsource
* */
public class BaseSelectProvider implements ProviderMethodResolver {

    /*
     * --------------------------------基础方法
     * */
    public static String selectById(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "select " +tableInfo.getSelectColumns()+
                " from " +tableInfo.getTableName()+
                " where " +tableInfo.getKeyColumn()+"=#{" +tableInfo.getKeyProperty()+ "}";
    }
    public static String selectListByIds(
            MybatisProviderContext context,
            @Param("collection") Collection<? extends Serializable> idList
    ){
        TableInfo tableInfo = context.getTableInfo();

        StringBuilder sqlBuf = new StringBuilder()
                .append("select ").append(tableInfo.getSelectColumns())
                .append(" from ").append( tableInfo.getTableName());
        if(MybatisCollectionUtils.isEmpty(idList)){
            //如果参数为空，则返回一个查不出任何结果的sql
            return sqlBuf.append(SqlProviderHelper.STATIC_FALSE_WHERE).toString();
        }

        return sqlBuf.append(SqlProviderHelper.parseIdCollectionWherePart(context,idList)).toString();
    }
    public static String selectListByPage(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "select " + tableInfo.getSelectColumns() +
                " from " + tableInfo.getTableName();
    }

    public static String selectCountAll(
            MybatisProviderContext context
    ){
        return "select count(1) "+
                " from " + context.getTableName();
    }

    public static String selectListByFk(
            MybatisProviderContext context,
            @Param("collection") Collection<? extends Serializable> fkCollection
    ){
        TableInfo tableInfo = context.getTableInfo();
        StringBuilder sqlBuf = new StringBuilder()
                .append("select ").append(tableInfo.getSelectColumns())
                .append(" from ").append( tableInfo.getTableName());

        if(MybatisCollectionUtils.isEmpty(fkCollection)){
            //如果参数为空，则返回一个查不出任何结果的sql
            return sqlBuf.append(SqlProviderHelper.STATIC_FALSE_WHERE).toString();
        }else if(tableInfo.getFkField()==null){
            throw new MybatisException("Could not find fkField on table.Possibly no @TableFk in Entity.");
        }

        return sqlBuf
                .append(SqlProviderHelper.STATIC_WHERE_SQL_FRAGMENT)
                .append(tableInfo.getFkColumn()).append(" in (")
                .append(context.formatCollection2ParameterMappings(tableInfo.getFkProperty(),fkCollection))
                .append(" ) ").toString();
    }
    /*
     * --------------------------------基础方法--------------------------------
     * */
    /*
    * --------------------------------whereLogic查询部分
    * */
    /*
     * 由于mybatis解析Provider方法的问题,多写一个参数，规避解析不正确的问题
     * */
    public static String selectListByWhereLogic(
            MybatisProviderContext context,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            @Param("param1") Object obj
    ){
        return doSelectByWhereLogic(context,whereLogic,context.getSelectColumns());
    }

    /*
     * 由于mybatis解析Provider方法的问题,多写一个参数，规避解析不正确的问题
     * */
    public static String selectCountByWhereLogic(
            MybatisProviderContext context,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            @Param("param1") Object obj
    ){
        return doSelectByWhereLogic(context,whereLogic,"COUNT(1)");
    }

    private static String doSelectByWhereLogic(
            MybatisProviderContext context,
            Object whereLogic,
            String selectCols
    ){
        return " select " + selectCols +
                " from " + context.getTableName() +
                SqlProviderHelper.parseWhereLogic(context, whereLogic);
    }
    /*
     * --------------------------------whereLogic查询部分--------------------------------
     * */

}
