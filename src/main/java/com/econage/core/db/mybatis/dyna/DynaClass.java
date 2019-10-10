package com.econage.core.db.mybatis.dyna;


import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class DynaClass implements Serializable {

    public DynaClass(
            final String clsName,
            final String idProperty,
            final String tableDef,
            final Collection<DynaProperty> properties
    ) {
        this.clsName = clsName;
        this.idProperty = idProperty;
        this.tableDef = tableDef;
        if (properties != null) {
            propertiesMap.clear();
            for (DynaProperty property : properties) {
                propertiesMap.put(property.getName(), property);
            }
        }
    }


    // ----------------------------------------------------- Instance Variables
    protected final String clsName;

    protected final String tableDef;

    protected final String idProperty;

    protected HashMap<String, DynaProperty> propertiesMap = new HashMap<>();
    // ------------------------------------------------------ DynaClass Methods
    public String getClsName() {
        return this.clsName;
    }

    public String getTableDef() {
        return tableDef;
    }

    public DynaProperty getDynaProperty(final String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("No property name specified");
        }
        return propertiesMap.get(name);
    }

    public Collection<DynaProperty> getDynaProperties() {
        return ImmutableList.copyOf(propertiesMap.values());
    }

    public DynaBean newInstance(){
            return new DynaBean(this);
    }
}
