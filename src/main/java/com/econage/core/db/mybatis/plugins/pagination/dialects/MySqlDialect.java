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
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * MYSQL 数据库分页语句组装实现
 * </p>
 */
public class MySqlDialect implements IDialect {

    public static final MySqlDialect INSTANCE = new MySqlDialect();

    @Override
    public String buildPaginationSql(PaginationContext paginationContext) {
        StringBuilder sqlBuilder = new StringBuilder(paginationContext.getOriginalSql());
        String orderStr = paginationContext.getOrderColumn();
        if(StringUtils.isNotEmpty(orderStr)){
            sqlBuilder.append(" order by ").append(orderStr);
        }
        sqlBuilder.append(" limit ").append(paginationContext.getOffset()).append(",").append(paginationContext.getLimit());
        return sqlBuilder.toString();
    }
}
