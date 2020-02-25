package com.econage.core.db.mybatis.enums;

public enum SqlMethod {

    /**
     * 插入
     */
    INSERT("insert", "插入一条数据（选择字段插入）", "INSERT INTO %s %s VALUES %s"),
    INSERT_ALL_COLUMN("insertAllColumn", "插入一条数据（全部字段插入）", "INSERT INTO %s %s VALUES %s");

    /**
     * 删除
     */
    /*DELETE_BY_ID("deleteById", "根据ID 删除一条数据", "DELETE FROM %s WHERE %s=#{%s}"),
    DELETE_BY_IDS("deleteByIds", "根据ID集合，批量删除数据", "DELETE FROM %s WHERE %s IN (%s)"),
    DELETE_BY_FK("deleteByFk", "根据外键，批量删除数据", "DELETE FROM %s WHERE %s = %s"),
    DELETE_BY_WHERE_LOGIC("deleteByWhereLogic", "根据ID集合，批量删除数据", "DELETE FROM %s WHERE %s IN (%s)"),*/

    /**
     * 修改
     */
    /*UPDATE_BY_ID("updateById", "根据ID 选择修改数据", "UPDATE %s %s WHERE %s=#{%s} %s"),
    UPDATE_ALL_COLUMN_BY_ID("updateAllColumnById", "根据ID 修改全部数据", "UPDATE %s %s WHERE %s=#{%s} %s"),
    UPDATE_PARTIAL_COLUMN_BY_ID("updatePartialColumnById", "根据部分列限定条件，更新记录", "UPDATE %s %s %s"),
    UPDATE_BATCH_BY_WHERE_LOGIC("updateBatchByWhereLogic", "根据 where条件 批量修改数据", "UPDATE %s %s WHERE %s"),
    UPDATE_BATCH_ALL_COLUMN_BY_WHERE_LOGIC("updateBatchAllColumnByWhereLogic", "根据 where条件 批量修改全部数据", "UPDATE %s %s WHERE %s"),
    UPDATE_BATCH_PARTIAL_COLUMN_BY_WHERE_LOGIC("updateBatchPartialColumnByWhereLogic", "根据部分列限定、where条件，批量更新记录", "UPDATE %s %s WHERE %s"),*/

    /**
     * 查询
     */
    /*SELECT_BY_ID("selectById", "根据ID 查询一条数据", "SELECT %s FROM %s WHERE %s=#{%s}"),
    SELECT_LIST_BY_IDS("selectListByIds", "根据ID集合，批量查询数据", "SELECT %s FROM %s WHERE %s IN (%s)"),
    SELECT_LIST_BY_PAGE("selectListByPage", "按照主键分页显示，会自动侦测主键信息", "SELECT %s FROM %s %s"),
    SELECT_COUNT_ALL("selectCountAll","获取表格总数","select count(1) from %s"),
    SELECT_LIST_BY_FK("selectListByFk", "按照外键信息，查询数据", "SELECT %s FROM %s %s"),
    SELECT_LIST_BY_WHERE_LOGIC("selectListByWhereLogic", "按照 where条件 ，自动组装查询条件", "SELECT %s FROM %s where %s"),
    SELECT_COUNT_BY_WHERE_LOGIC("selectCountByWhereLogic","按照 where条件 ，自动组装查询条件返回计数结果","SELECT COUNT(1) FROM %s where %s");*/

    private final String method;
    private final String desc;
    private final String sql;

    SqlMethod(final String method, final String desc, final String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return this.method;
    }

    public String getDesc() {
        return this.desc;
    }

    public String getSql() {
        return this.sql;
    }
}
