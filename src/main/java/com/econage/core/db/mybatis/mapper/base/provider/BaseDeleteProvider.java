package com.econage.core.db.mybatis.mapper.base.provider;

import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.strengthen.MybatisProviderContext;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

import java.io.Serializable;
import java.util.Collection;

import static com.econage.core.db.mybatis.mapper.MapperConst.WHERE_LOGIC_PARAM_NAME;

public class BaseDeleteProvider implements ProviderMethodResolver {

    public static String deleteById(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "delete from " + tableInfo.getTableName()+
                " where " + tableInfo.getKeyColumn()+"=#{" + tableInfo.getKeyProperty()+"}";
    }

    public static String deleteByIds(
            MybatisProviderContext context,
            @Param("collection") Collection<? extends Serializable> idList
    ){
        if(MybatisCollectionUtils.isEmpty(idList)){
            throw new IllegalArgumentException("parameter is empty!");
        }
        return "delete from " +context.getTableName()+
                SqlProviderHelper.parseIdCollectionWherePart(context,idList);
    }

    public static String deleteByFk(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "delete from " + tableInfo.getTableName()+
               " where " + tableInfo.getFkColumn()+ "=#{" +tableInfo.getFkProperty()+"}";
    }

    public static String deleteByWhereLogic(
            MybatisProviderContext context,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    ){
        return "delete from "+
                context.getTableName()+
                SqlProviderHelper.parseWhereLogic(context, whereLogic);
    }
}
