package com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity;


import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;

public class DynaClass implements Serializable {



    // ----------------------------------------------------- Instance Variables
    protected final String clsName;

    protected final String tableDef;

    protected final String idColumn;

    protected final String fkColumn;

    protected LinkedHashMap<String, DynaColumn> columnsMap = new LinkedHashMap<>();
    // ------------------------------------------------------ DynaClass Methods
    public String getClsName() {
        return this.clsName;
    }

    public String getTableDef() {
        return tableDef;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public String getFkColumn() {
        return fkColumn;
    }

    public DynaClass(
            final String clsName,
            final String idColumn,
            final String fkColumn,
            final String tableDef,
            final Collection<DynaColumn> properties
    ) {
        this.clsName = clsName;
        this.idColumn = idColumn;
        this.fkColumn = fkColumn;
        this.tableDef = tableDef;
        if (properties != null) {
            for (DynaColumn property : properties) {
                columnsMap.put(property.getName(), property);
            }
        }
    }

    public DynaColumn getDynaProperty(final String name) {
        if (MybatisStringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("No property name specified");
        }
        return columnsMap.get(name);
    }

    public Collection<DynaColumn> getDynaColumns() {
        return columnsMap.values();
    }

    public DynaBean newInstance(){
            return new DynaBean(this);
    }
}
