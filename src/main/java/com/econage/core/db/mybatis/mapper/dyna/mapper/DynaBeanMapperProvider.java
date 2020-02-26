package com.econage.core.db.mybatis.mapper.dyna.mapper;

import com.econage.core.db.mybatis.mapper.dyna.entity.DynaBean;
import com.econage.core.db.mybatis.mapper.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.mapper.dyna.entity.DynaColumn;
import com.econage.core.db.mybatis.mapper.dyna.wherelogic.DynaWhereLogic;
import com.econage.core.db.mybatis.mapper.provider.MybatisProviderContext;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import java.io.Serializable;
import java.util.Collection;

import static com.econage.core.db.mybatis.mapper.MapperConst.*;

public class DynaBeanMapperProvider implements ProviderMethodResolver {

    private static String formatPropertyInBoundSQL(String dynaPropertyName){
        return DYNA_ENTITY_PARAM_NAME+"_"+dynaPropertyName;
    }

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return int
     */
    public static String insert(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    ){
        SQL sql = new SQL().INSERT_INTO(dynaClass.getTableDef());

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
            if(propertyVal!=null){
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.VALUES(dynaColumnName, MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }
        }

        return sql.toString();
    }

    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     * @return int
     */
    public static String deleteById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_PARAM_NAME) Serializable id
    ){
        return new SQL()
                .DELETE_FROM(dynaClass.getTableDef())
                .WHERE( dynaClass.getIdColumn()+" = "+ MybatisSqlUtils.formatBoundParameter(ID_PARAM_NAME) )
                .toString();
    }

    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param idList 主键ID列表
     * @return int
     */
    public static String deleteByIds(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    ){
        String idsFragment = context.formatCollection2ParameterMappings(ID_COLLECTION_PARAM_NAME,idList);
        return new SQL()
                .DELETE_FROM(dynaClass.getTableDef())
                .WHERE(dynaClass.getIdColumn()+" in ("+idsFragment+")")
                .toString();
    }

    /**
     * <p>
     * 删除（根据外键 批量删除）
     * </p>
     *
     * @param fk 外键ID
     * @return int
     */
    public static String deleteByFk(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(FK_PARAM_NAME) Serializable fk
    ){
        return new SQL()
                .DELETE_FROM(dynaClass.getTableDef())
                .WHERE( dynaClass.getFkColumn()+" = "+ MybatisSqlUtils.formatBoundParameter(FK_PARAM_NAME) )
                .toString();
    }

    /**
     * <p>
     * 删除（根据 where 条件批量删除）
     * </p>
     *
     * @param whereLogic where逻辑
     * @return int
     */
    public static String deleteByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());
        return new SQL()
                .DELETE_FROM(dynaClass.getTableDef())
                .WHERE(whereLogic.getBoundSqlArray())
                .toString();
    }

    /**
     * <p>
     * 根据 ID 修改更新非空的列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    public static String updateById(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
            if(StringUtils.equals(idColumn,dynaColumnName)){
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.WHERE(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }else{
                if(propertyVal!=null){
                    context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                    sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
                }
            }
        }

        return sql.toString();
    }

    /**
     * <p>
     * 根据 ID 修改更新全部列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    public static String updateAllColumnById(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
            if(StringUtils.equals(idColumn,dynaColumnName)){
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.WHERE(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }else{
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }
        }

        return sql.toString();
    }

    /*
     * 根据id更新特定列
     * */
    public static String updatePartialColumnById(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(DYNA_COLUMN_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
            if(StringUtils.equals(idColumn,dynaColumnName)){
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.WHERE(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }else{
                if(propertyNameArray.contains(dynaColumnName)&&propertyVal!=null){
                    context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                    sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
                }
            }
        }

        return sql.toString();
    }

    /**
     * <p>
     * 根据 where 逻辑修改更新非空的列
     * </p>
     *
     * @param entity set语句部分对应的值
     * @param whereLogic where逻辑
     * @return int
     */
    public static String updateBatchByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            if(!StringUtils.equals(idColumn,dynaColumnName)&&propertyVal!=null){
                String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }
        }

        sql.WHERE(whereLogic.getBoundSqlArray());
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());

        return sql.toString();
    }

    /**
     * <p>
     * 根据 where逻辑 修改更新全部列
     * </p>
     *
     *
     * @param entity set语句部分对应的值
     * @param whereLogic where逻辑
     * @return int
     */
    public static String updateBatchAllColumnByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            if(!StringUtils.equals(idColumn,dynaColumnName)){
                String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }
        }

        sql.WHERE(whereLogic.getBoundSqlArray());
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());

        return sql.toString();
    }

    /*
     * <p>
     * 根据where条件更新特定列
     * </p>
     *
     * @param entity set语句部分对应的值
     * @param propertyNameArray 限定使用的entity的属性
     * @param whereLogic where逻辑
     * @return int
     * */
    public static String updateBatchPartialColumnByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(DYNA_COLUMN_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        SQL sql = new SQL().UPDATE(dynaClass.getTableDef());
        String idColumn = dynaClass.getIdColumn();

        for(DynaColumn dynaColumn : dynaClass.getDynaColumns()){
            String dynaColumnName = dynaColumn.getName();
            Object propertyVal = entity.get(dynaColumn.getName());
            if(!StringUtils.equals(idColumn,dynaColumnName)&&propertyVal!=null&&propertyNameArray.contains(dynaColumnName)){
                String propertyInBoundSQL = formatPropertyInBoundSQL(dynaColumnName);
                context.setAdditionalParam(propertyInBoundSQL,propertyVal);
                sql.SET(dynaColumnName+"="+ MybatisSqlUtils.formatBoundParameter(propertyInBoundSQL));
            }
        }

        sql.WHERE(whereLogic.getBoundSqlArray());
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());

        return sql.toString();
    }

    /**
     * <p>
     * 根据 ID 查询
     * </p>
     *
     * @param id 主键ID
     * @return T
     */
    public static String selectById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_PARAM_NAME) Serializable id
    ){
        return new SQL()
                .SELECT(dynaClass.getDynaColumns().stream().map(DynaColumn::getName).toArray(String[]::new))
                .FROM(dynaClass.getTableDef())
                .WHERE( dynaClass.getIdColumn()+" = "+ MybatisSqlUtils.formatBoundParameter(ID_PARAM_NAME) )
                .toString();
    }

    /**
     * <p>
     * 查询（根据ID 批量查询）
     * </p>
     *
     * @param idList 主键ID列表
     * @return List<T>
     */
    public static String selectListByIds(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    ){
        String idsFragment = context.formatCollection2ParameterMappings(ID_COLLECTION_PARAM_NAME,idList);
        return new SQL()
                .SELECT(dynaClass.getDynaColumns().stream().map(DynaColumn::getName).toArray(String[]::new))
                .FROM(dynaClass.getTableDef())
                .WHERE(dynaClass.getIdColumn()+" in ("+idsFragment+")")
                .toString();
    }

    /*
     * 按照主键分页显示，会自动侦测主键信息
     * */
    public static String selectListByPage(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass
    ){
        return new SQL()
                .SELECT(dynaClass.getDynaColumns().stream().map(DynaColumn::getName).toArray(String[]::new))
                .FROM(dynaClass.getTableDef())
                .toString();
    }

    /*
     * 获取总数
     * */
    public static String selectCountAll(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass
    ){
        return new SQL()
                .SELECT("count(1)")
                .FROM(dynaClass.getTableDef())
                .toString();
    }

    /*
     *  按照外键信息，显示
     * */
    public static String selectListByFk(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(FK_COLLECTION_PARAM_NAME) Collection<? extends Serializable> fkCollection
    ){
        String fksFragment = context.formatCollection2ParameterMappings(FK_COLLECTION_PARAM_NAME,fkCollection);
        return new SQL()
                .SELECT(dynaClass.getDynaColumns().stream().map(DynaColumn::getName).toArray(String[]::new))
                .FROM(dynaClass.getTableDef())
                .WHERE(dynaClass.getIdColumn()+" in ("+fksFragment+")")
                .toString();
    }

    /*
     * 通用查询方法，可以做分页查询
     * */
    public static String selectListByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());
        return new SQL()
                .SELECT(dynaClass.getDynaColumns().stream().map(DynaColumn::getName).toArray(String[]::new))
                .FROM(dynaClass.getTableDef())
                .WHERE(whereLogic.getBoundSqlArray())
                .toString();
    }

    /*
     * 通用计数方法
     * */
    public static String selectCountByWhereLogic(
            MybatisProviderContext context,
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) DynaWhereLogic whereLogic
    ){
        context.getAdditionalParam().putAll(whereLogic.getBoundParams());
        return new SQL()
                .SELECT("count(1)")
                .FROM(dynaClass.getTableDef())
                .WHERE(whereLogic.getBoundSqlArray())
                .toString();
    }
}
