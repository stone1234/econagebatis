package com.econage.core.db.mybatis.mapper.dyna.entity;


import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class DynaClass implements Serializable {

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
            columnsMap.clear();
            for (DynaColumn property : properties) {
                columnsMap.put(property.getName(), property);
            }
        }
    }


    // ----------------------------------------------------- Instance Variables
    protected final String clsName;

    protected final String tableDef;

    protected final String idColumn;

    protected final String fkColumn;

    protected HashMap<String, DynaColumn> columnsMap = new HashMap<>();
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

    public DynaColumn getDynaProperty(final String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("No property name specified");
        }
        return columnsMap.get(name);
    }

    public Collection<DynaColumn> getDynaColumns() {
        return ImmutableList.copyOf(columnsMap.values());
    }

    public DynaBean newInstance(){
            return new DynaBean(this);
    }
}
