/**
 *    Copyright 2017-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.econage.core.db.mybatis.mapper;

import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.sqlsource.basic.*;
import com.econage.core.db.mybatis.mapper.sqlsource.AbstractDefaultMethodSqlSource;
import com.econage.core.db.mybatis.mapper.sqlsource.bywhere.DefaultDeleteByWhereMethodSqlSource;
import com.econage.core.db.mybatis.mapper.sqlsource.bywhere.DefaultSelectByWhereMethodSqlSource;
import com.econage.core.db.mybatis.mapper.sqlsource.bywhere.DefaultUpdateByWhereMethodSqlSource;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.google.common.base.Preconditions;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;

//在解析一个mapper的时候会调用注入器
public class SqlInjector {
    private static final Log logger = LogFactory.getLog(SqlInjector.class);


    private final MybatisGlobalAssistant globalAssistant;
    public SqlInjector(MybatisGlobalAssistant globalAssistant) {
        this.globalAssistant = globalAssistant;
    }


    /**
     * <p>
     * 注入SQL
     * </p>
     *
     * @param builderAssistant
     * @param mapperClass
     */
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        Preconditions.checkNotNull(mapperClass,"mapperclass is null!");
        //注入器只为BaseMapper的接口注入方法
        if (!globalAssistant.isMapperCached(mapperClass)
           &&BaseMapper.class.isAssignableFrom(mapperClass)) {
            TableInfo table = globalAssistant.saveAndGetTableInfoByMapper(mapperClass);
            if (table != null) {
                injectSql(builderAssistant, table);
            }
        }
    }

    protected void injectSql(MapperBuilderAssistant builderAssistant,TableInfo table) {
        /*
         * #148 表信息包含主键，注入主键相关方法
         */
        if (MybatisStringUtils.isNotEmpty(table.getKeyProperty())) {
            /* 删除 */
            this.injectDeleteByIdSql(builderAssistant,table,false);
            this.injectDeleteByIdSql(builderAssistant,table,true );
            this.injectDeleteByFkSql(builderAssistant,table);
            /* 修改 */
            this.injectUpdateByIdSql(builderAssistant, table,false,false);
            this.injectUpdateByIdSql(builderAssistant, table,true,false);
            this.injectUpdateByIdSql(builderAssistant, table,false,true);
            /* 查询 */
            this.injectSelectByIdSql(builderAssistant,table,false,false);
            this.injectSelectByIdSql(builderAssistant,table,true,false);
            this.injectSelectByIdSql(builderAssistant,table,false,true);
            this.injectSelectCountAllSql(builderAssistant,table);
            this.injectSelectByFkSQL(builderAssistant,table);
        } else {
            // 表不包含主键时 给予警告
            logger.warn(String.format(
                    "%s ,Not found @TableId annotation, Cannot use 'xxById' Method.",
                    builderAssistant.getCurrentNamespace()
            ));
        }
        /* 插入 */
        this.injectInsertOneSql(builderAssistant, table, false);
        this.injectInsertOneSql(builderAssistant, table, true);


        /*
        * 带where逻辑sql方法
        * */
        this.injectDeleteByWhereLogic(builderAssistant,table);
        this.injectSelectByWhereSQL(builderAssistant,table);
        this.injectUpdateByWhereLogic(builderAssistant, table,false,false);
        this.injectUpdateByWhereLogic(builderAssistant, table,true,false);
        this.injectUpdateByWhereLogic(builderAssistant, table,false,true);
    }


    protected void injectInsertOneSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,boolean selective
    ){
        DefaultInsertMethodSqlSource sqlSource = new DefaultInsertMethodSqlSource(
                globalAssistant.getConfiguration(),
                table,selective
        );

        KeyGenerator keyGenerator = sqlSource.saveAndGetKeyGenerator(builderAssistant);
        if(keyGenerator==null){
            //如果没有keyGenerator，则由DefaultInsertMethodSqlSource在执行时插入
            this.addMappedStatement(
                    builderAssistant,
                    sqlSource,
                    table.getModelClass(),
                    null, Integer.class,
                    NoKeyGenerator.INSTANCE, null, null
            );
        }else{
            this.addMappedStatement(
                    builderAssistant,
                    sqlSource,
                    table.getModelClass(),
                    null, Integer.class,
                    keyGenerator, table.getKeyProperty(), table.getKeyColumn()
            );
        }

    }


    protected void injectSelectByIdSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,boolean batch,boolean page
    ){
        addSelectStatement(
                builderAssistant,
                table,
                new DefaultSelectMethodSqlSource(
                        globalAssistant.getConfiguration(),
                        table,batch,page
                )
        );
    }
    protected void injectSelectCountAllSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table
    ){
        this.addMappedStatement(
                builderAssistant,
                new DefaultSelectCountMethodSqlSource(globalAssistant.getConfiguration(), table),
                null, null,Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    protected void injectSelectByFkSQL(
            MapperBuilderAssistant builderAssistant,
            TableInfo table
    ){
        addSelectStatement(
                builderAssistant,
                table,
                new DefaultSelectByFkMethodSqlSource(globalAssistant.getConfiguration(), table)
        );
    }

    protected void injectSelectByWhereSQL(
            MapperBuilderAssistant builderAssistant,
            TableInfo table
    ){
        addSelectStatement(
                builderAssistant,
                table,
                new DefaultSelectByWhereMethodSqlSource(globalAssistant.getConfiguration(), table,false)
        );

        addMappedStatement(
                builderAssistant,
                new DefaultSelectByWhereMethodSqlSource(globalAssistant.getConfiguration(), table,true),
                null, null,Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }


    private void addSelectStatement(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,
            AbstractDefaultMethodSqlSource sqlSource
    ){
        String resultMap = table.getDefaultSelectResultMap();
        Class<?> resultType = null;
        if(MybatisStringUtils.isEmpty(resultMap)){
            resultType = table.getModelClass();
        }
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                null, resultMap,resultType,
                NoKeyGenerator.INSTANCE, null, null
        );
    }


    protected void injectUpdateByIdSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,
            boolean selective,boolean partial
    ) {
        DefaultUpdateMethodSqlSource sqlSource = new DefaultUpdateMethodSqlSource(
                globalAssistant.getConfiguration(),
                table,selective,partial
        );
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                table.getModelClass(), null, Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    protected void injectUpdateByWhereLogic(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,
            boolean selective,boolean partial
    ){
        DefaultUpdateByWhereMethodSqlSource sqlSource = new DefaultUpdateByWhereMethodSqlSource(
                globalAssistant.getConfiguration(),
                table,selective,partial
        );
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                table.getModelClass(), null, Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    protected void injectDeleteByIdSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table,boolean batch
    ) {
        DefaultDeleteMethodSqlSource sqlSource = new DefaultDeleteMethodSqlSource(globalAssistant.getConfiguration(), table,batch);
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                null, null, Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    protected void injectDeleteByFkSql(
            MapperBuilderAssistant builderAssistant,
            TableInfo table
    ){
        DefaultDeleteByFkMethodSqlSource sqlSource = new DefaultDeleteByFkMethodSqlSource(globalAssistant.getConfiguration(), table);
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                null, null, Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    protected void injectDeleteByWhereLogic(
            MapperBuilderAssistant builderAssistant,
            TableInfo table
    ){
        DefaultDeleteByWhereMethodSqlSource sqlSource = new DefaultDeleteByWhereMethodSqlSource(globalAssistant.getConfiguration(), table);
        this.addMappedStatement(
                builderAssistant,
                sqlSource,
                null, null, Integer.class,
                NoKeyGenerator.INSTANCE, null, null
        );
    }

    public MappedStatement addMappedStatement(
            MapperBuilderAssistant builderAssistant,
            AbstractDefaultMethodSqlSource sqlSource,
            Class<?> parameterClass,
            String resultMap, Class<?> resultType,
            KeyGenerator keyGenerator, String keyProperty, String keyColumn
    ) {
        String mappedStatementId = builderAssistant.getCurrentNamespace() + "." + sqlSource.getMethodId();
        if (globalAssistant.getConfiguration().hasStatement(mappedStatementId)) {
            System.err.println("{" + mappedStatementId
                    + "} Has been loaded by XML or SqlProvider, ignoring the injection of the SQL.");
            return null;
        }
		/* 缓存逻辑处理 */
        boolean isSelect = false;
        if (sqlSource.getSqlCommandType() == SqlCommandType.SELECT) {
            isSelect = true;
        }

        return builderAssistant.addMappedStatement(
                mappedStatementId,
                sqlSource,
                StatementType.PREPARED,
                sqlSource.getSqlCommandType(),
                null,
                null,
                null,
                parameterClass,
                resultMap,
                resultType,
                null,
                !isSelect,
                isSelect,
                false,
                keyGenerator,
                keyProperty,
                keyColumn,
                null,
                globalAssistant.getConfiguration().getLanguageDriver(null),
                null
        );
    }


}
