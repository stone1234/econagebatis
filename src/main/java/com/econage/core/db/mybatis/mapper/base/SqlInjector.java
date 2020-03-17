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
package com.econage.core.db.mybatis.mapper.base;

import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.mapper.BaseMapper;
import com.econage.core.db.mybatis.mapper.base.insert.AbstractMethodSqlSource;
import com.econage.core.db.mybatis.mapper.base.insert.DefaultInsertMethodSqlSource;
import com.econage.core.db.mybatis.util.MybatisPreconditions;
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
        MybatisPreconditions.checkNotNull(mapperClass,"mapperclass is null!");
        //注入器只为BaseMapper的接口注入方法
        if (!globalAssistant.isMapperParsed(mapperClass)
           && BaseMapper.class.isAssignableFrom(mapperClass)) {
            TableInfo table = globalAssistant.saveAndGetTableInfoByMapper(mapperClass);
            if (table != null) {
                injectSql(builderAssistant, table);
            }
        }
    }

    /*
    * 除新增外，其他方法由注解解决
    * 新增方法需要处理序列号逻辑
    * */
    protected void injectSql(MapperBuilderAssistant builderAssistant,TableInfo table) {
        /* 插入 */
        this.injectInsertOneSql(builderAssistant, table, false);
        this.injectInsertOneSql(builderAssistant, table, true);
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



    public MappedStatement addMappedStatement(
            MapperBuilderAssistant builderAssistant,
            AbstractMethodSqlSource sqlSource,
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
