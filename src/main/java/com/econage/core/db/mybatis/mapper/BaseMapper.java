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

import com.econage.core.db.mybatis.entity.BaseEntity;
import com.econage.core.db.mybatis.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Mapper 继承该接口后，无需编写 mapper.xml 文件，即可获得CRUD功能
 */
public interface BaseMapper<T extends BaseEntity> {
    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return int
     */
    Integer insert(T entity);

    /**
     * <p>
     * 插入一条记录
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    Integer insertAllColumn(T entity);

    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     * @return int
     */
    Integer deleteById(Serializable id);

    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param idList 主键ID列表
     * @return int
     */
    Integer deleteByIds(Collection<? extends Serializable> idList);

    /**
     * <p>
     * 删除（根据外键 批量删除）
     * </p>
     *
     * @param fk 外键ID
     * @return int
     */
    Integer deleteByFk(Serializable fk);

    /**
     * <p>
     * 删除（根据 where 条件批量删除）
     * </p>
     *
     * @param whereLogic where逻辑
     * @return int
     */
    Integer deleteByWhereLogic(Object whereLogic);

    /**
     * <p>
     * 根据 ID 修改更新非空的列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    Integer updateById(T entity);

    /**
     * <p>
     * 根据 ID 修改更新全部列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    Integer updateAllColumnById(T entity);

    /*
    * 根据id更新特定列
    * */
    Integer updatePartialColumnById(
            @Param("et") T entity,
            @Param("pna") Collection<String> propertyNameArray
    );

    /**
     * <p>
     * 根据 where 逻辑修改更新非空的列
     * 批量更新需要注意乐观锁字段的处理，此时谓语部分只依据whereLogic参数，没有加上乐观锁信息
     * 更新后，所有更新过的行会使用一个新的版本号
     * </p>
     *
     * @param entity set语句部分对应的值
     * @param whereLogic where逻辑
     * @return int
     */
    Integer updateBatchByWhereLogic(
            @Param("et") T entity,
            @Param("wl") Object whereLogic
    );

    /**
     * <p>
     * 根据 where逻辑 修改更新全部列
     * 批量更新需要注意乐观锁字段的处理，此时谓语部分只依据whereLogic参数，没有加上乐观锁信息
     * 更新后，所有更新过的行会使用一个新的版本号
     * </p>
     *
     *
     * @param entity set语句部分对应的值
     * @param whereLogic where逻辑
     * @return int
     */
    Integer updateBatchAllColumnByWhereLogic(
            @Param("et") T entity,
            @Param("wl") Object whereLogic
    );

    /*
     * <p>
     * 根据where条件更新特定列
     * 批量更新需要注意乐观锁字段的处理，此时谓语部分只依据whereLogic参数，没有加上乐观锁信息
     * 更新后，所有更新过的行会使用一个新的版本号
     * </p>
     *
     * @param entity set语句部分对应的值
     * @param propertyNameArray 限定使用的entity的属性
     * @param whereLogic where逻辑
     * @return int
     * */
    Integer updateBatchPartialColumnByWhereLogic(
            @Param("et") T entity,
            @Param("pna") Collection<String> propertyNameArray,
            @Param("wl") Object whereLogic
    );

    /**
     * <p>
     * 根据 ID 查询
     * </p>
     *
     * @param id 主键ID
     * @return T
     */
    T selectById(Serializable id);

    /**
     * <p>
     * 查询（根据ID 批量查询）
     * </p>
     *
     * @param idList 主键ID列表
     * @return List<T>
     */
    List<T> selectListByIds(Collection<? extends Serializable> idList);

    /*
    * 按照主键分页显示，会自动侦测主键信息
    * */
    List<T> selectListByPage(Pagination pagination);

    /*
    * 获取总数
    * */
    Integer selectCountAll();

    /*
    *  按照外键信息，显示
    * */
    List<T> selectListByFk(Collection<? extends Serializable> fkCollection,Pagination page);

    /*
    * 通用查询方法，可以做分页查询
    * */
    List<T> selectListByWhereLogic(Object searForm, Pagination page);

    /*
    * 通用计数方法
    * */
    Integer selectCountByWhereLogic(Object searForm);

}
