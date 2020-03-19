package com.econage.core.db.mybatis.mapper.base;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableFieldInfo;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.enums.FieldStrategy;
import com.econage.core.db.mybatis.enums.IdType;
import com.econage.core.db.mybatis.mapper.provider.MybatisProviderContext;
import com.econage.core.db.mybatis.uid.dbincrementer.IKeyGenerator;
import com.econage.core.db.mybatis.util.MybatisCollectionUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parsing.PropertyParser;

import java.io.Serializable;
import java.util.ArrayList;
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
            Collection<? extends Serializable> idList
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
                canUse = MybatisStringUtils.isNotEmpty((String)propertyVal);
            }else{
                canUse = propertyVal!=null;
            }
        }
        return canUse;
    }


    //todo
    public static KeyGenerator parseKeyGenerator(
            String mappedStatementId,
            TableInfo tableInfo,
            MybatisConfiguration configuration,
            MapperBuilderAssistant builderAssistant
    ){
        KeyGenerator keyGenerator = null;
        /*
         * 仅在通过sequence或者数据库自增时，才需要为mybatis提供主键信息
         * 判断，是否需要在解析时，注入sql
         * true:插入前注入id值
         * false:由mybatis处理id值
         * */
        if (tableInfo!=null&&MybatisStringUtils.isNotEmpty(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /* 自增主键,mysql、sql server可用 */
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
            }else if(null != tableInfo.getKeySequence()){
                keyGenerator = createAndSaveKeyGenerator(
                        mappedStatementId,
                        tableInfo,
                        configuration,
                        builderAssistant
                );
            }
        }

        return keyGenerator;
    }
    /*
    * 创建调用sequence的sql映射
    * */
    private static KeyGenerator createAndSaveKeyGenerator(
            String mappedStatementId,
            TableInfo tableInfo,
            MybatisConfiguration configuration,
            MapperBuilderAssistant builderAssistant
    ) {
        IKeyGenerator keyGenerator = configuration.getGlobalAssistant().getKeyGenerator();
        if (null == keyGenerator) {
            throw new IllegalArgumentException("not configure IKeyGenerator implementation class.");
        }
        String selectKeyGeneratorId = mappedStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Class<?> resultTypeClass = tableInfo.getKeySequence().clazz();
        String keyProperty = tableInfo.getKeyProperty();
        String keyColumn = tableInfo.getKeyColumn();

        String sequenceSQL = keyGenerator.executeSql(tableInfo.getKeySequence().value());
        SqlSource sqlSource = new StaticSqlSource(
                configuration,
                PropertyParser.parse(sequenceSQL, configuration.getVariables()),
                new ArrayList<>()
        );

        builderAssistant.addMappedStatement(
                selectKeyGeneratorId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                null, null,
                null, null,
                null, resultTypeClass, null,
                false, false, false,
                NoKeyGenerator.INSTANCE, keyProperty, keyColumn,
                null,
                configuration.getLanguageDriver(null),
                null
        );
        selectKeyGeneratorId = builderAssistant.applyCurrentNamespace(selectKeyGeneratorId, false);
        MappedStatement keyStatement = configuration.getMappedStatement(selectKeyGeneratorId, false);
        SelectKeyGenerator selectKeyGenerator = new SelectKeyGenerator(keyStatement, true);
        configuration.addKeyGenerator(selectKeyGeneratorId, selectKeyGenerator);
        return selectKeyGenerator;
    }


}
