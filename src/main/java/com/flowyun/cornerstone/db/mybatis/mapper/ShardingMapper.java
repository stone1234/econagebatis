package com.flowyun.cornerstone.db.mybatis.mapper;

import com.flowyun.cornerstone.db.mybatis.entity.BasicEntity;
import com.flowyun.cornerstone.db.mybatis.mapper.providerimpl.DeleteProviderImpl;
import com.flowyun.cornerstone.db.mybatis.mapper.providerimpl.InsertProviderImpl;
import com.flowyun.cornerstone.db.mybatis.mapper.providerimpl.SelectProviderImpl;
import com.flowyun.cornerstone.db.mybatis.mapper.providerimpl.UpdateProviderImpl;
import com.flowyun.cornerstone.db.mybatis.pagination.Pagination;
import org.apache.ibatis.annotations.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.flowyun.cornerstone.db.mybatis.mapper.MapperConst.*;

public interface ShardingMapper<T extends BasicEntity> {

    /**
     * 插入一条记录
     *
     * @param rnTab 表名
     * @param entity 实体对象
     * @return int
     */
    @InsertProvider(InsertProviderImpl.class)
    Integer insert(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity
    );

    /**
     * <p>
     * 插入一条记录
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    @InsertProvider(InsertProviderImpl.class)
    Integer insertAllColumn(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity
    );

    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     * @return int
     */
    @DeleteProvider(DeleteProviderImpl.class)
    Integer deleteById(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ID_PARAM_NAME) Serializable id
    );

    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param idList 主键ID列表
     * @return int
     */
    @DeleteProvider(DeleteProviderImpl.class)
    Integer deleteByIds(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    );

    /**
     * <p>
     * 删除（根据外键 批量删除）
     * </p>
     *
     * @param fk 外键ID
     * @return int
     */
    @DeleteProvider(DeleteProviderImpl.class)
    Integer deleteByFk(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(FK_PARAM_NAME) Serializable fk
    );

    /**
     * <p>
     * 删除（根据 where 条件批量删除）
     * </p>
     *
     * @param whereLogic where逻辑
     * @return int
     */
    @DeleteProvider(DeleteProviderImpl.class)
    Integer deleteByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    );

    /**
     * <p>
     * 根据 ID 修改更新非空的列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updateById(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity
    );

    /**
     * <p>
     * 根据 ID 修改更新全部列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updateAllColumnById(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity
    );

    /*
     * 根据id更新特定列
     * */
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updatePartialColumnById(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity,
            @Param(PROPERTY_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray
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
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updateBatchByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
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
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updateBatchAllColumnByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
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
    @UpdateProvider(UpdateProviderImpl.class)
    Integer updateBatchPartialColumnByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ENTITY_PARAM_NAME) T entity,
            @Param(PROPERTY_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    );

    /**
     * <p>
     * 根据 ID 查询
     * </p>
     *
     * @param id 主键ID
     * @return T
     */
    @SelectProvider(SelectProviderImpl.class)
    T selectById(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ID_PARAM_NAME) Serializable id
    );

    /**
     * <p>
     * 查询（根据ID 批量查询）
     * </p>
     *
     * @param idList 主键ID列表
     * @return List<T>
     */
    @SelectProvider(SelectProviderImpl.class)
    List<T> selectListByIds(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    );

    /*
     * 按照主键分页显示，会自动侦测主键信息
     * */
    @SelectProvider(SelectProviderImpl.class)
    List<T> selectListByPage(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            Pagination pagination
    );

    /*
     * 获取总数
     * */
    @SelectProvider(SelectProviderImpl.class)
    Integer selectCountAll(@Param(RUNTIME_TABLE_NAME) String rnTab);

    /*
     *  按照外键信息，显示
     * */
    @SelectProvider(SelectProviderImpl.class)
    List<T> selectListByFk(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> fkCollection,
            Pagination page
    );

    /*
     * 通用查询方法，可以做分页查询
     * */
    @SelectProvider(SelectProviderImpl.class)
    List<T> selectListByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            Pagination page
    );

    /*
     * 通用计数方法
     * */
    @SelectProvider(SelectProviderImpl.class)
    Integer selectCountByWhereLogic(
            @Param(RUNTIME_TABLE_NAME) String rnTab,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    );

}
