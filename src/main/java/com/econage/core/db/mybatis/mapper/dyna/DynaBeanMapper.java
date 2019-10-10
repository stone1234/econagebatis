package com.econage.core.db.mybatis.mapper.dyna;

import com.econage.core.db.mybatis.dyna.DynaBean;
import com.econage.core.db.mybatis.dyna.DynaClass;
import com.econage.core.db.mybatis.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.econage.core.db.mybatis.mapper.MapperConst.*;

public interface DynaBeanMapper {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return int
     */
    Integer insert(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    );

    /**
     * <p>
     * 插入一条记录
     * </p>
     *
     * @param entity 实体对象
     * @return int
     */
    Integer insertAllColumn(
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
    Integer updateAllColumnById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity
    );

    /*
     * 根据id更新特定列
     * */
    Integer updatePartialColumnById(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
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
    Integer updateBatchPartialColumnByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(DYNA_ENTITY_PARAM_NAME) DynaBean entity,
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
    List<DynaBean> selectListByIds(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(ID_COLLECTION_PARAM_NAME) Collection<? extends Serializable> idList
    );

    /*
     * 按照主键分页显示，会自动侦测主键信息
     * */
    List<DynaBean> selectListByPage(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            Pagination pagination
    );

    /*
     * 获取总数
     * */
    Integer selectCountAll(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass
    );

    /*
     *  按照外键信息，显示
     * */
    List<DynaBean> selectListByFk(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(FK_COLLECTION_PARAM_NAME) Collection<? extends Serializable> fkCollection,
             Pagination pagination
    );

    /*
     * 通用查询方法，可以做分页查询
     * */
    List<DynaBean> selectListByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic,
            Pagination page
    );

    /*
     * 通用计数方法
     * */
    Integer selectCountByWhereLogic(
            @Param(DYNA_CLASS_PARAM_NAME) DynaClass dynaClass,
            @Param(WHERE_LOGIC_PARAM_NAME) Object whereLogic
    );
}
