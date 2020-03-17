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

import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 简单分页模型
 */
public class Pagination extends RowBounds implements Serializable {

    public static Pagination newPagination(){
        return new Pagination();
    }
    //很多前端组件，传回后台使用页数、页面行数描述分页，此处可用
    public static Pagination newPaginationWithPageRows(int page,int rows){
        return new Pagination()
                .paginationLimit(rows)
                .paginationOffset( (page-1)*rows );
    }

    private int paginationOffset;
    private int paginationLimit;

    /**
     * <p>
     * SQL 排序 ORDER BY 字段，例如： id DESC（根据id倒序查询）
     * 可以组合排序
     * </p>
     * <p>
     * DESC 表示按倒序排序(即：从大到小排序)<br>
     * ASC 表示按正序排序(即：从小到大排序)
     * </p>
     */
    private String[] sortName;

    /**
     * 是否为升序 ASC（ 默认： true ）
     * 各个拍序列顺序
     */
    private String[] sortOrder;

    private Pagination() {
        super();
    }

    public Pagination rowBounds(RowBounds rowBounds){
        setPaginationLimit(rowBounds.getLimit());
        setPaginationOffset(rowBounds.getOffset());
        return this;
    }

    public Pagination paginationLimit(int rows){
        setPaginationLimit(rows);
        return this;
    }

    public Pagination paginationOffset(int page){
        setPaginationOffset(page);
        return this;
    }

    public Pagination sortName(String[] sort){
        setSortName(sort);
        return this;
    }

    public Pagination sortOrder(String[] order){
        setSortOrder(order);
        return this;
    }

    public int getPaginationOffset() {
        return paginationOffset;
    }

    public void setPaginationOffset(int paginationOffset) {
        this.paginationOffset = paginationOffset;
    }

    public int getPaginationLimit() {
        return paginationLimit;
    }

    public void setPaginationLimit(int paginationLimit) {
        this.paginationLimit = paginationLimit;
    }

    public String[] getSortName() {
        return sortName;
    }

    public void setSortName(String... sortName) {
        this.sortName = sortName;
    }

    public String[] getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String... sortOrder) {
        this.sortOrder = sortOrder;
    }

    /*作废RowBounds两个参数*/
    @Override
    public int getOffset() {
        return NO_ROW_OFFSET;
    }

    @Override
    public int getLimit() {
        return NO_ROW_LIMIT;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "paginationOffset" + "=" + paginationOffset + "," +
                "paginationLimit" + "=" + paginationLimit + "," +
                "sort[]" + "=" + Arrays.toString(sortName) + "," +
                "order[]" + "=" + Arrays.toString(sortOrder) +
                "}";
    }

}
