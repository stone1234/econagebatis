package com.flowyun.cornerstone.db.mybatis.mapper.providerimpl;

import com.flowyun.cornerstone.db.mybatis.MybatisException;
import com.flowyun.cornerstone.db.mybatis.entity.TableFieldInfo;
import com.flowyun.cornerstone.db.mybatis.entity.TableInfo;
import com.flowyun.cornerstone.db.mybatis.enums.IdType;
import com.flowyun.cornerstone.db.mybatis.mapper.provider.MybatisProviderContext;
import com.flowyun.cornerstone.db.mybatis.util.MybatisSqlUtils;
import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;
import com.flowyun.cornerstone.db.mybatis.uuid.IdWorker;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.reflection.MetaObject;

import java.util.ArrayList;
import java.util.List;

import static com.flowyun.cornerstone.db.mybatis.mapper.MapperConst.ENTITY_PARAM_NAME;

public class InsertProviderImpl implements ProviderMethodResolver {


    public static String insert(
            MybatisProviderContext context,
            @Param(ENTITY_PARAM_NAME) Object entity,
            @Param("param1") Object obj
    ){
        return doParseInsertSQL(context,entity,true);
    }

    public static String insertAllColumn(
            MybatisProviderContext context,
            @Param(ENTITY_PARAM_NAME) Object entity,
            @Param("param1") Object obj
    ){
        return doParseInsertSQL(context,entity,false);
    }


    public static String doParseInsertSQL(
            MybatisProviderContext context,
            Object entity,
            boolean selective
    ){

        //变量准备
        TableInfo tableInfo = context.getTableInfo();
        MetaObject entityMetaObject = context.newMetaObject(entity);
        List<String> columns = new ArrayList<>();
        List<String> parameterTokens = new ArrayList<>();

        handleVersion(tableInfo,entityMetaObject);
        handleId(tableInfo,entityMetaObject,columns,parameterTokens);

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();
            //如果字段在默认的插入方法中无效
            if(!fieldInfo.isDefaultInsert()){
                continue;
            }

            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(property);
            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }

            if(!selective|| SqlProviderHelper.useFieldInModifySql(fieldInfo,propertyType,propertyVal)){
                columns.add(fieldInfo.getColumn());
                parameterTokens.add("#{"+ENTITY_PARAM_NAME+"."+fieldInfo.getEl()+"}");
            }
        }


        return  "insert into " +context.getRuntimeTableName()+
                " ( " + MybatisSqlUtils.commaJoin(columns) +" ) " +
                " values ( " +MybatisSqlUtils.commaJoin(parameterTokens)+" )";
    }

    /*
    * 尝试填充版本号
    * */
    private static void handleVersion(
            TableInfo tableInfo,
            MetaObject entityMetaObject
    ){
        //填充乐观锁值
        TableFieldInfo versionField = tableInfo.getVersionField();
        if(versionField!=null){
            String property = versionField.getProperty();
            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(versionField.getProperty());
            if(!MybatisStringUtils.isCharSequence(propertyType)){
                throw new MybatisException("version property type error,expected:CharSequence,actual["+property+"]!");
            }
            if(propertyVal==null){
                entityMetaObject.setValue(versionField.getProperty(), IdWorker.getIdStr());
            }
        }
    }
    /*
    * 尝试填充uid、uuid类型主键，获取id插入列
    * */
    private static void handleId(
            TableInfo tableInfo,
            MetaObject entityMetaObject,
            List<String> columns,
            List<String> parameterTokens
    ){
        if (MybatisStringUtils.isEmpty(tableInfo.getKeyProperty())) {
            return;
        }else if (tableInfo.getIdType() == IdType.AUTO||tableInfo.getIdType()==null||null != tableInfo.getKeySequence()) {
            /* 自增主键 不做任何操作，生成的插入语句也不需要描述列信息,mysql,ms sql server场景 */
            return;
        }

        //通过sequence等数据库查询方式获取的主键，由mybatis框架负责注入id值
        //其他uuid方式，则在此处注入主键，如果主键属性已经有值，则不注入
        Object idValue = entityMetaObject.getValue(tableInfo.getKeyProperty());
        if (MybatisStringUtils.checkValNull(idValue)) {
            if (tableInfo.getIdType() == IdType.ID_WORKER) {
                entityMetaObject.setValue(tableInfo.getKeyProperty(), IdWorker.getIdStr());
            } else if (tableInfo.getIdType() == IdType.UUID) {
                entityMetaObject.setValue(tableInfo.getKeyProperty(), IdWorker.get32UUID());
            }
        }

        columns.add(tableInfo.getKeyColumn());
        parameterTokens.add("#{"+ ENTITY_PARAM_NAME+"."+tableInfo.getKeyProperty()+"}");
    }
}
