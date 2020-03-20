package com.econage.core.db.mybatis.wherelogic;

import com.econage.core.db.mybatis.util.MybatisStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 查询表单信息，将查询表单中的字段映射为where查询条件，可以解决大多数sql查询
 * </p>
 */
public class WhereLogicInfo {

    private String className;
    /*字段where映射*/
    private Map<String, WhereLogicFieldInfo> propertyWhereMap = new HashMap<>();

    private List<WhereLogicFieldInfo> fieldInfos = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void putSearchFieldInfo(WhereLogicFieldInfo formFieldInfo){
        if(formFieldInfo==null){
            return;
        }
        propertyWhereMap.put(formFieldInfo.getProperty(),formFieldInfo);
        fieldInfos.add(formFieldInfo);
    }

    public String getFieldWhereLogic(String property){
        if(MybatisStringUtils.isEmpty(property)){
            return MybatisStringUtils.EMPTY;
        }
        WhereLogicFieldInfo formFieldInfo = propertyWhereMap.get(property);
        if(formFieldInfo==null){
            return MybatisStringUtils.EMPTY;
        }
        return formFieldInfo.getWhereLogic();
    }

    public boolean isPrimitiveInProperty(String property){
        if(MybatisStringUtils.isEmpty(property)){
            throw new IllegalArgumentException(" property is null! ");
        }
        WhereLogicFieldInfo formFieldInfo = propertyWhereMap.get(property);
        if(formFieldInfo==null){
            throw new IllegalArgumentException(" No field info! ");
        }
        return formFieldInfo.isPrimitiveType();
    }

    public List<WhereLogicFieldInfo> getFieldInfos() {
        return fieldInfos;
    }
}
