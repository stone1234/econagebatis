package com.econage.core.db.mybatis.dyna.wherelogic;

import java.util.Map;

public class DynaWherePart {
    private final String boundSQL;
    private final Map<String,Object> boundParams;

    public DynaWherePart(String boundSQL, Map<String, Object> boundParams) {
        this.boundSQL = boundSQL;
        this.boundParams = boundParams;
    }

    public String getBoundSQL() {
        return boundSQL;
    }

    public Map<String, Object> getBoundParams() {
        return boundParams;
    }
}
