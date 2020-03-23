package com.flowyun.cornerstone.db.mybatis.mapper.providerimpl;

import com.flowyun.cornerstone.db.mybatis.MybatisException;
import com.flowyun.cornerstone.db.mybatis.entity.TableFieldInfo;
import com.flowyun.cornerstone.db.mybatis.entity.TableInfo;
import com.flowyun.cornerstone.db.mybatis.mapper.provider.MybatisProviderContext;
import com.flowyun.cornerstone.db.mybatis.util.MybatisSqlUtils;
import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;
import com.flowyun.cornerstone.db.mybatis.uuid.IdWorker;
import com.flowyun.cornerstone.db.mybatis.mapper.MapperConst;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UpdateProviderImpl implements ProviderMethodResolver {

    /*
     * -------------------------------------------基本方法
     * */
    /*
     * 由于mybatis解析Provider方法的问题,多写一个参数，规避解析不正确的问题
     * */
    public static String updateById(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param("param1") Object obj
    ){
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> setParts = parseSqlSetPartByEntity(context.getTableInfo(),entityMetaObject,true);

        return doUpdateById(context,entityMetaObject,setParts);
    }
    /*
     * 由于mybatis解析Provider方法的问题,多写一个参数，规避解析不正确的问题
     * */
    public static String updateAllColumnById(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param("param1") Object obj
    ){
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> setParts = parseSqlSetPartByEntity(context.getTableInfo(),entityMetaObject,false);
        return doUpdateById(context,entityMetaObject,setParts);
    }
    public static String updatePartialColumnById(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param(MapperConst.PROPERTY_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray
    ){
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> setParts = parseSqlSetPartByPartialCol(context.getTableInfo(),entityMetaObject,propertyNameArray);
        return doUpdateById(context,entityMetaObject,setParts);
    }

    private static String doUpdateById(
            MybatisProviderContext context,
            MetaObject entityMetaObject,
            List<String> setParts
    ){
        TableInfo tableInfo = context.getTableInfo();

        String pkColWherePart = tableInfo.getKeyColumn()+"=#{" + MapperConst.ENTITY_PARAM_NAME+"." + tableInfo.getKeyProperty()+"}";
        String wherePart;
        TableFieldInfo versionField =  context.getTableInfo().getVersionField();
        if(versionField!=null){
            List<String> whereParts = new ArrayList<>();
            whereParts.add(pkColWherePart);
            handleVersion2SetOnBasic(context,entityMetaObject,versionField,setParts,whereParts);
            wherePart = MybatisSqlUtils.wherePartJoin(SqlProviderHelper.STATIC_WHERE_SQL_FRAGMENT,whereParts);
        }else{
            wherePart = SqlProviderHelper.STATIC_WHERE_SQL_FRAGMENT+pkColWherePart;
        }

        return " update " + context.getRuntimeTableName() +
                " set " + MybatisSqlUtils.commaJoin(setParts)+
                wherePart;
    }

    //处理基础方法中，乐观锁的逻辑：set部分使用新时间戳；where部分使用旧时间戳；entity回填新时间戳
    private static void handleVersion2SetOnBasic(
            MybatisProviderContext context,
            MetaObject entityMetaObject,
            TableFieldInfo versionField,
            List<String> sqlSetsPart,
            List<String> sqlWherePart
    ){
        //set version-->new_version
        String property = versionField.getProperty();
        String currVersionProperty = property+ MybatisSqlUtils.CURR_VERSION_STAMP_SUFFIX;
        Class<?> propertyType = entityMetaObject.getGetterType(property);
        Object propertyVal = entityMetaObject.getValue(property);

        if(propertyVal==null){
            throw new MybatisException("version is null,field:["+property+"]!");
        }else if(MybatisStringUtils.isCharSequence(propertyType)){
            if(MybatisStringUtils.isEmpty((String)propertyVal)){
                throw new MybatisException("version is null or empty,field:["+property+"]!");
            }
        }

        String newVersionProperty = property+ MybatisSqlUtils.NEW_VERSION_STAMP_SUFFIX;
        String newVersionStamp = IdWorker.getIdStr();

        context.setAdditionalParam(newVersionProperty, newVersionStamp);
        context.setAdditionalParam(currVersionProperty,propertyVal);

        //set部分使用新时间戳
        sqlSetsPart.add(versionField.getColumn()+"=#{"+newVersionProperty+"}");
        //where部分使用旧时间戳
        sqlWherePart.add(versionField.getColumn()+"=#{"+currVersionProperty+"}");
        //entity回填新时间戳
        entityMetaObject.setValue(property,newVersionStamp);
    }
    /*
     * -------------------------------------------基本方法-------------------------------------------
     * */

    /*
    * -------------------------------------------whereLogic
    * */
    public static String updateBatchByWhereLogic(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param(MapperConst.WHERE_LOGIC_PARAM_NAME) Object whereLogic
    ){
        return doUpdateBatchByWhereLogic(true,context,entity,whereLogic);
    }

    public static String updateBatchAllColumnByWhereLogic(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param(MapperConst.WHERE_LOGIC_PARAM_NAME) Object whereLogic
    ){
        return doUpdateBatchByWhereLogic(false,context,entity,whereLogic);
    }

    private static String doUpdateBatchByWhereLogic(
            boolean selective,
            MybatisProviderContext context,
            Object entity,
            Object whereLogic
    ){
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> setParts = parseSqlSetPartByEntity(context.getTableInfo(),entityMetaObject,selective);
        handleVersion2SetOnBatch(context,entityMetaObject,setParts);

        return " update " + context.getRuntimeTableName() +
                " set " + MybatisSqlUtils.commaJoin(setParts)+
                SqlProviderHelper.parseWhereLogic(context,whereLogic);
    }

    public static String updateBatchPartialColumnByWhereLogic(
            MybatisProviderContext context,
            @Param(MapperConst.ENTITY_PARAM_NAME) Object entity,
            @Param(MapperConst.PROPERTY_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray,
            @Param(MapperConst.WHERE_LOGIC_PARAM_NAME) Object whereLogic
    ){
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> setParts = parseSqlSetPartByPartialCol(context.getTableInfo(),entityMetaObject,propertyNameArray);
        handleVersion2SetOnBatch(context,entityMetaObject,setParts);

        return " update " + context.getRuntimeTableName() +
                " set " + MybatisSqlUtils.commaJoin(setParts)+
                SqlProviderHelper.parseWhereLogic(context,whereLogic);
    }
    //填充version相关set部分，批量更新的时候，只更新版本号，批量更新不在谓语条件中限定版本号
    private static void handleVersion2SetOnBatch(
            MybatisProviderContext context,
            MetaObject entityMetaObject,
            List<String> setParts
    ){
        TableFieldInfo versionField =  context.getTableInfo().getVersionField();
        if(versionField==null){
            return;
        }

        //set version-->new_version
        String property = versionField.getProperty();
        String newVersionProperty = property + MybatisSqlUtils.NEW_VERSION_STAMP_SUFFIX;
        String newVersionStamp = IdWorker.getIdStr();

        context.setAdditionalParam(newVersionProperty, newVersionStamp);
        //实体类回填新版本号，批量环境，似乎可以忽略，不需要回填
        entityMetaObject.setValue(property,newVersionStamp);

        //set部分使用新的时间戳属性名，属性写入了additionalParam，优先级较高
        setParts.add(versionField.getColumn()+"=#{"+newVersionProperty+"}");
    }
    /*
     * -------------------------------------------whereLogic-------------------------------------------
     * */

    /*
    * 根据entity属性，决定更新的列，仅处理普通列，忽略乐观锁列
    * */
    private static List<String> parseSqlSetPartByEntity(
            TableInfo tableInfo,
            MetaObject entityMetaObject,
            /*entity是否选择性插入*/
            boolean selective
    ){
        List<String> sqlSetsPart = new ArrayList<>();
        TableFieldInfo versionField = tableInfo.getVersionField();

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            //DefaultUpdate为设置为false，则不更新
            if(!fieldInfo.isDefaultUpdate()){
                continue;
            }
            String property = fieldInfo.getProperty();
            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(property);
            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }

            /*
            * 如果要更新全部列或者可选更新列同时useFieldInModifySql根据策略允许更新
            * */
            if(!selective|| SqlProviderHelper.useFieldInModifySql(fieldInfo,propertyType,propertyVal)){
                //如果插入列是乐观锁列，则不在此处处理
                if (versionField == null || !property.equals(versionField.getProperty())) {
                    //如果使用自动映射需要添加前缀（et）
                    sqlSetsPart.add(fieldInfo.getColumn()+"=#{"+ MapperConst.ENTITY_PARAM_NAME+"."+fieldInfo.getEl()+"}");
                }
            }
        }
        return sqlSetsPart;
    }

    /*
     * 根据传入的特定列名，决定要更新的列，仅处理普通列，忽略乐观锁列
     * */
    private static List<String> parseSqlSetPartByPartialCol(
            TableInfo tableInfo,
            MetaObject entityMetaObject,
            /*entity是否选择性插入*/
            Collection<String> partialProperty
    ){
        List<String> sqlSetsPart = new ArrayList<>();
        TableFieldInfo versionField = tableInfo.getVersionField();

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();
            //以业务特定列是否包含这个属性为准，忽略DefaultUpdate配置
            if(!partialProperty.contains(property)){
                continue;
            }
            Class<?> propertyType = entityMetaObject.getGetterType(property);
            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }
            //如果插入列是乐观锁列，则不在此处处理
            if (versionField == null || !property.equals(versionField.getProperty())) {
                //如果使用自动映射需要添加前缀（et）
                sqlSetsPart.add(fieldInfo.getColumn()+"=#{"+ MapperConst.ENTITY_PARAM_NAME+"."+fieldInfo.getEl()+"}");
            }
        }
        return sqlSetsPart;
    }


}
