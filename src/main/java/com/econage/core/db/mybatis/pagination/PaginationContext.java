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
package com.econage.core.db.mybatis.pagination;

import com.econage.core.db.mybatis.enums.DBType;
import com.econage.core.db.mybatis.util.MybatisArrayUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.util.MybatisStringUtils;

import java.util.ArrayList;
import java.util.List;

public class PaginationContext {
    private final String originalSql;
    private final Pagination pagination;
    private final DBType dbType;
    //绝大多数场景下，不需要由方言类处理排序，在mysql中，由原sql处理即可
    //在sql层面需要替换的行号或者是偏移量，由方言类决定

    //原有sql参数之前插入的参数
    private List<Object> paginationParamBefore;
    //原有sql参数之后插入的参数
    private List<Object> paginationParamAfter;

    public PaginationContext(String originalSql, Pagination pagination, DBType dbType) {
        this.originalSql = originalSql;
        this.pagination = pagination;
        this.dbType = dbType;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public String getOrderColumn(){
        if(!isSortNumConsistent()){
            throw new IllegalStateException("order and sort not inconsistent!");
        }
        String[] sortArray = pagination.getSortName();
        String[] orderArray = pagination.getSortOrder();
        if(MybatisArrayUtils.isEmpty(sortArray)){
            return MybatisStringUtils.EMPTY;
        }
        List<String> sortColumnWithOrder = new ArrayList<>(sortArray.length);
        for(int i=0,l=sortArray.length;i<l;i++){
            if(MybatisArrayUtils.isNotEmpty(orderArray)){
                sortColumnWithOrder.add(sortArray[i]+" "+orderArray[i]);
            }else{
                sortColumnWithOrder.add(sortArray[i]);
            }
        }
        return MybatisSqlUtils.commaJoin(sortColumnWithOrder);
    }

    public int getLimit(){
        return pagination.getPaginationLimit();
    }

    public int getOffset() {
        return pagination.getPaginationOffset();
    }

    public boolean isSortNumConsistent(){
        return MybatisArrayUtils.isEmpty(pagination.getSortName())
             ||MybatisArrayUtils.isEmpty(pagination.getSortOrder())
             ||pagination.getSortName().length==pagination.getSortOrder().length;
    }

    public void addPaginationParamBefore(Object param) {
        if(this.paginationParamBefore==null){
            this.paginationParamBefore = new ArrayList<>();
        }
        paginationParamBefore.add(param);
    }

    public void addPaginationParamAfter(Object param) {
        if(this.paginationParamAfter==null){
            this.paginationParamAfter = new ArrayList<>();
        }
        paginationParamAfter.add(param);
    }

    public List<Object> getPaginationParamBefore() {
        return paginationParamBefore;
    }

    public List<Object> getPaginationParamAfter() {
        return paginationParamAfter;
    }

    public DBType getDbType() {
        return dbType;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
