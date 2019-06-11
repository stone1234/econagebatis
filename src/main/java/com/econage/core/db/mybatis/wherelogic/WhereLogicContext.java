package com.econage.core.db.mybatis.wherelogic;

import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

public class WhereLogicContext {
    public static WhereLogicContext newContext(
            Map<String, Object> additionalMap,
            Object whereLogicObj
    ){
        if(additionalMap==null){
            throw new IllegalArgumentException("additionalMap is null!");
        }
        if(whereLogicObj==null){
            throw new IllegalArgumentException("whereLogicObj is null!");
        }
        return new WhereLogicContext(additionalMap, whereLogicObj);
    }

    private final Map<String,Object> additionalMap;
    private final Object whereLogicObj;

    private WhereLogicContext(Map<String, Object> additionalMap, Object whereLogicObj) {
        this.additionalMap = additionalMap;
        this.whereLogicObj = whereLogicObj;
    }

    public Object getWhereLogicObj() {
        return whereLogicObj;
    }

    public void putAdditionalParam(String param, Object object){
        additionalMap.put(param,object);
    }

    public String parseCollection(String itemName, Collection<?> typeParams){

        return MybatisSqlUtils.formatCollection2ParameterMappings(
                StringUtils.EMPTY,StringUtils.EMPTY,itemName,
                typeParams,
                additionalMap
        );
    }
}
