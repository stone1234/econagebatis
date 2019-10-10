package com.econage.core.db.mybatis.mapper.sqlsource.bywhere;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableFieldInfo;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.SqlMethod;
import com.econage.core.db.mybatis.mapper.MapperConst;
import com.econage.core.db.mybatis.mapper.sqlsource.SqlProviderBinding;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.uuid.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultUpdateByWhereMethodSqlSource extends AbstractByWhereMethodSqlSource {

    public static final String UPDATE_SQL_TPL = "UPDATE %s SET %s WHERE %s ";

    private final boolean selective;
    private final boolean partial;

    public DefaultUpdateByWhereMethodSqlSource(
            MybatisConfiguration configuration,
            TableInfo tableInfo,
            boolean selective, boolean partial
    ) {
        super(configuration, tableInfo);
        this.selective = selective;
        this.partial = partial;
    }

    @Override
    protected SqlProviderBinding parseBinding(Object parameterObject) {
        if(selective&&partial){
            throw new IllegalStateException("selective and partial both true!");
        }
        /*---------------------1，解析参数*/
        Map<String, Object> params = (Map<String, Object>) parameterObject;
        parameterObject = params.get(MapperConst.ENTITY_PARAM_NAME);
        Object whereLogic = params.get(MapperConst.WHERE_LOGIC_PARAM_NAME);
        Collection<String> validProperty = partial?((Collection<String>)params.get(MapperConst.PROPERTY_NAME_ARRAY_PARAM_NAME)):null;
        /*---------------------2，解析set部分，并尝试填充版本字段*/
        MetaObject entityMetaObject = getConfiguration().newMetaObject(parameterObject);
        Map<String,Object> additionalMap = Maps.newHashMap();
        List<String> setPart = sqlSet(entityMetaObject, validProperty, additionalMap);

        String whereLogicSQL = parseWhereLogicJoinSQL(whereLogic,additionalMap);

        String updateSql = String.format(
                UPDATE_SQL_TPL,
                tableInfo.getTableName(),
                MybatisSqlUtils.commaJoin(setPart),
                whereLogicSQL
        );

        return SqlProviderBinding.of(updateSql,additionalMap);
    }


    private List<String> sqlSet(
            MetaObject entityMetaObject,
            Collection<String> partialProperty,
            Map<String,Object> additionalParam
    ) {
        List<String> sqlSetsPart = Lists.newArrayList();
        TableFieldInfo versionField = tableInfo.getVersionField();
        boolean versionResolved = false;

        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();

            boolean shouldHandleProperty;
            if(partialProperty!=null){
                //如果业务特定列不为空，则以业务特定列是否包含这个属性为准，忽略DefaultUpdate配置
                shouldHandleProperty = partialProperty.contains(property);
            }else{
                //如果业务特定列为空，则以DefaultUpdate为准
                shouldHandleProperty = fieldInfo.isDefaultUpdate();
            }
            if(!shouldHandleProperty){
                continue;
            }

            Class<?> propertyType = entityMetaObject.getGetterType(property);
            Object propertyVal = entityMetaObject.getValue(property);

            if(propertyType!=fieldInfo.getPropertyType()){
                throw new IllegalArgumentException("inconsistent parameter property type");
            }

            boolean putSet = false;
            if(selective){
                if(useFieldInModifySql(fieldInfo,propertyType,propertyVal)){
                    putSet = true;
                }
            }else{
                putSet = true;
            }

            if(putSet){
                if(versionField!=null&&property.equals(versionField.getProperty())){
                    //如果插入列是乐观锁列
                    appendNewVersion2Set(
                            entityMetaObject,
                            versionField,
                            sqlSetsPart,
                            additionalParam
                    );
                    versionResolved = true;
                }else{
                    sqlSetsPart.add(fieldInfo.getColumn()+"=#{"+fieldInfo.getEl()+"}");
                    //如果使用自动映射需要添加前缀（et），此处将数据提取，存入additionalParam，以便移动映射到sql中的参数中
                    additionalParam.put(property,propertyVal);
                }
            }
        }

        if(versionField!=null&&!versionResolved){
            appendNewVersion2Set(
                    entityMetaObject,
                    versionField,
                    sqlSetsPart,
                    additionalParam
            );
        }

        return sqlSetsPart;
    }



    //填充version相关set部分，批量更新的时候，只更新版本号，但不在谓语条件中限定版本号
    private void appendNewVersion2Set(
            MetaObject entityMetaObject,
            TableFieldInfo versionField,
            List<String> sqlSetsPart,
            Map<String, Object> additionalParam
    ){
        //set version-->new_version
        String property = versionField.getProperty(),
                newVersionProperty = property+ MybatisSqlUtils.NEW_VERSION_STAMP_SUFFIX;

        String newVersionStamp = IdWorker.getIdStr();
        sqlSetsPart.add(versionField.getColumn()+"=#{"+newVersionProperty+"}");
        additionalParam.put(newVersionProperty, newVersionStamp);

        //实体类回填新版本号
        entityMetaObject.setValue(property,newVersionStamp);
    }

    @Override
    public String getMethodId() {
        if(partial){
            return SqlMethod.UPDATE_BATCH_PARTIAL_COLUMN_BY_WHERE_LOGIC.getMethod();
        }else if(selective){
            return SqlMethod.UPDATE_BATCH_BY_WHERE_LOGIC.getMethod();
        }else{
            return SqlMethod.UPDATE_BATCH_ALL_COLUMN_BY_WHERE_LOGIC.getMethod();
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UPDATE;
    }
}
