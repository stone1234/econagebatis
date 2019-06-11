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
 * MYSQL 8 数据库分页语句组装实现
 * 语法基本与oracle一致
 * </p>
 */
public class MySql8Dialect extends OracleDialect {

    public static final MySql8Dialect INSTANCE = new MySql8Dialect();

    public static final String paginationTpl =
            " select * from ( " +
                    " select tmp_table__.*,row_number() OVER(order by %s) as tmp_rn__ " +
                    " from ( %s ) tmp_table__ " +
                    " ) tmp_table_rn__ " +
                    " where tmp_rn__ BETWEEN ? and ? " +
                    " order by tmp_rn__ ";

    @Override
    public String buildPaginationSql(PaginationContext paginationContext) {
        //排序列，在原有sql参数之前
        /*paginationContext.addPaginationParamBefore(
                paginationContext.getOrderColumn()
        );*/
        //开始行结束行，在原有sql参数之后
        paginationContext.addPaginationParamAfter(
                paginationContext.getOffset()+1
        );
        paginationContext.addPaginationParamAfter(
                paginationContext.getOffset()+paginationContext.getLimit())
        ;

        return String.format(
                paginationTpl,
                paginationContext.getOrderColumn(),
                paginationContext.getOriginalSql()
        );
    }

}
