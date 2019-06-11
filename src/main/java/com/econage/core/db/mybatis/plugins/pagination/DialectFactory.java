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
package com.econage.core.db.mybatis.plugins.pagination;

import com.econage.core.db.mybatis.enums.DBType;
import com.econage.core.db.mybatis.plugins.pagination.dialects.*;
import com.econage.core.db.mybatis.util.MybatisStringUtils;
import com.econage.core.db.mybatis.MybatisException;

/**
 * <p>
 * 分页方言工厂类
 * </p>
 */
public class DialectFactory {

    /**
     * Physical Pagination Interceptor for all the queries with parameter
     * {@link org.apache.ibatis.session.RowBounds}
     * @param dialectClazz
     * @return
     * @throws Exception
     */
    public static String buildPaginationSql(PaginationContext paginationContext,String dialectClazz)
            throws Exception {
        IDialect dialect = getDialect(paginationContext.getDbType(), dialectClazz);
        return dialect.buildPaginationSql(paginationContext);
    }

    /**
     * <p>
     * 获取数据库方言
     * </p>
     *
     * @param dbType       数据库类型
     * @param dialectClazz 自定义方言实现类
     * @return
     * @throws Exception
     */
    private static IDialect getDialect(DBType dbType, String dialectClazz) throws Exception {
        IDialect dialect = null;
        if (MybatisStringUtils.isNotEmpty(dialectClazz)) {
            try {
                Class<?> clazz = Class.forName(dialectClazz);
                if (IDialect.class.isAssignableFrom(clazz)) {
                    dialect = (IDialect) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                throw new MybatisException("Class :" + dialectClazz + " is not found");
            }
        } else if (null != dbType) {
            dialect = getDialectByDbType(dbType);
        }
        /* 未配置方言则抛出异常 */
        if (dialect == null) {
            throw new MybatisException("The value of the dialect property in mybatis configuration.xml is not defined.");
        }
        return dialect;
    }

    /**
     * <p>
     * 根据数据库类型选择不同分页方言
     * </p>
     *
     * @param dbType 数据库类型
     * @return
     * @throws Exception
     */
    private static IDialect getDialectByDbType(DBType dbType) {
        IDialect dialect;
        switch (dbType) {
            case MYSQL8:
                dialect = MySql8Dialect.INSTANCE;
                break;
            case MYSQL:
                dialect = MySqlDialect.INSTANCE;
                break;
            case ORACLE:
                dialect = OracleDialect.INSTANCE;
                break;
            case H2:
                dialect = H2Dialect.INSTANCE;
                break;
            case POSTGRE:
                dialect = PostgreDialect.INSTANCE;
                break;
            case HSQL:
                dialect = HSQLDialect.INSTANCE;
                break;
            case SQLITE:
                dialect = SQLiteDialect.INSTANCE;
                break;
            default:
                throw new MybatisException("The Database's Not Supported! DBType:" + dbType);
        }
        return dialect;
    }

}
