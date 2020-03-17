package com.econage.core.db.mybatis.mapper;

import com.econage.core.db.mybatis.mapper.dyna.entity.DynaBean;
import com.econage.core.db.mybatis.mapper.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.mapper.dyna.mapper.DynaBeanMapperProvider;
import com.econage.core.db.mybatis.pagination.Pagination;
import org.apache.ibatis.annotations.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.econage.core.db.mybatis.mapper.MapperConst.*;

@Mapper
public interface DynaBeanMapper {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return int
     */
    @InsertProvider(DynaBeanMapperProvider.class)
    Integer insert(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    );

    /**
     * <p>
     * 根据 ID 删除
     * </p>
     *
     * @param id 主键ID
     * @return int
     */
    @DeleteProvider(DynaBeanMapperProvider.class)
    Integer deleteById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
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
    @DeleteProvider(DynaBeanMapperProvider.class)
    Integer deleteByIds(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
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
    @DeleteProvider(DynaBeanMapperProvider.class)
    Integer deleteByFk(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
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
    @DeleteProvider(DynaBeanMapperProvider.class)
    Integer deleteByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
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
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updateById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    );

    /**
     * <p>
     * 根据 ID 修改更新全部列
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updateAllColumnById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    );

    /*
     * 根据id更新特定列
     * */
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updatePartialColumnById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(DYNA_COLUMN_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray
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
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updateBatchByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
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
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updateBatchAllColumnByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
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
    @UpdateProvider(DynaBeanMapperProvider.class)
    Integer updateBatchPartialColumnByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
            @Param(DYNA_COLUMN_NAME_ARRAY_PARAM_NAME) Collection<String> propertyNameArray,
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
    @SelectProvider(DynaBeanMapperProvider.class)
    DynaBean selectById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
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
    @SelectProvider(DynaBeanMapperProvider.class)
    List<DynaBean> selectListByIds(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    );

    /*
     * 按照主键分页显示，会自动侦测主键信息
     * */
    @SelectProvider(DynaBeanMapperProvider.class)
    List<DynaBean> selectListByPage(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            Pagination pagination
    );

    /*
     * 获取总数
     * */
    @SelectProvider(DynaBeanMapperProvider.class)
    Integer selectCountAll(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass
    );

    /*
     *  按照外键信息，显示
     * */
    @SelectProvider(DynaBeanMapperProvider.class)
    List<DynaBean> selectListByFk(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(FK_COLLECTION_PARAM_NAME) Collection<? extends Serializable> fkCollection,
             Pagination pagination
    );

    /*
     * 通用查询方法，可以做分页查询
     * */
    @SelectProvider(DynaBeanMapperProvider.class)
    List<DynaBean> selectListByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            Pagination page
    );

    /*
     * 通用计数方法
     * */
    @SelectProvider(DynaBeanMapperProvider.class)
    Integer selectCountByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    );
}
