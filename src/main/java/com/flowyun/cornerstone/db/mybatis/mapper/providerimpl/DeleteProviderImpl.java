package com.flowyun.cornerstone.db.mybatis.mapper.providerimpl;

import com.flowyun.cornerstone.db.mybatis.entity.TableInfo;
import com.flowyun.cornerstone.db.mybatis.mapper.provider.MybatisProviderContext;
import com.flowyun.cornerstone.db.mybatis.util.MybatisCollectionUtils;
import com.flowyun.cornerstone.db.mybatis.mapper.MapperConst;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

import java.io.Serializable;
import java.util.Collection;

public class DeleteProviderImpl implements ProviderMethodResolver {

    public static String deleteById(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "delete from " + context.getRuntimeTableName() +
                " where " + tableInfo.getKeyColumn()+"=#{" + MapperConst.ID_PARAM_NAME +"}";
    }

    public static String deleteByIds(
            MybatisProviderContext context,
            @Param(MapperConst.ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    ){
        if(MybatisCollectionUtils.isEmpty(idList)){
            throw new IllegalArgumentException("parameter is empty!");
        }
        return "delete from " +context.getRuntimeTableName()+
                SqlProviderHelper.parseIdCollectionWherePart(context,idList);
    }

    public static String deleteByFk(
            MybatisProviderContext context
    ){
        TableInfo tableInfo = context.getTableInfo();
        return "delete from " + context.getRuntimeTableName()+
               " where " + tableInfo.getFkColumn()+ "=#{" + MapperConst.FK_PARAM_NAME +"}";
    }

    /*
    * 由于mybatis解析Provider方法的问题,多写一个参数，规避解析不正确的问题
    * */
    public static String deleteByWhereLogic(
            MybatisProviderContext context,
            @Param(MapperConst.WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            @Param("param1") Object obj
    ){
        return "delete from "+
                context.getRuntimeTableName()+
                SqlProviderHelper.parseWhereLogic(context, whereLogic);
    }
}
