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
package com.econage.core.db.mybatis.plugins.pagination.dialects;


import com.econage.core.db.mybatis.plugins.pagination.PaginationContext;

/**
 * <p>
 * H2 数据库分页方言
 * </p>
 */
public class H2Dialect implements IDialect {

    public static final H2Dialect INSTANCE = new H2Dialect();

    @Override
    public String buildPaginationSql(PaginationContext paginationContext) {
        StringBuilder sql = new StringBuilder(paginationContext.getOriginalSql());
        sql.append(" limit ").append(paginationContext.getLimit());
        if (paginationContext.getOffset() > 0) {
            sql.append(" offset ").append(paginationContext.getOffset());
        }
        return sql.toString();
    }
}