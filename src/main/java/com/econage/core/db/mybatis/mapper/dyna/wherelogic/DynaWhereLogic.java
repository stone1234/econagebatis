package com.econage.core.db.mybatis.mapper.dyna.wherelogic;

import com.econage.core.db.mybatis.mapper.MapperConst;
import com.econage.core.db.mybatis.util.MybatisArrayUtils;
import com.econage.core.db.mybatis.util.MybatisSqlUtils;
import com.econage.core.db.mybatis.uuid.IdWorker;

import java.util.*;

public class DynaWhereLogic {
    private static String newDynaBoundSQLParamName(){
        return MapperConst.DYNA_BOUND_PARAM+"_"+ IdWorker.getId();
    }

    private final List<String> boundSqlList = new ArrayList<>();
    private final Map<String,Object> boundParams = new HashMap<>();

    public DynaWhereLogic() {
    }

    /*
    * 列名等于数据的条件
    * */
    public void addColumnEqIntoWhere(String column,Object object){
        String dynaBoundParamName = newDynaBoundSQLParamName();
        boundSqlList.add(column+" = "+ MybatisSqlUtils.formatBoundParameter(dynaBoundParamName));
        boundParams.put(dynaBoundParamName,object);
    }
    /*
     * 列集合搜索
     * */
    public void addColumnInCollectionIntoWhere(String column, Collection<Object> params){
        List<String> boundSqlParams = new ArrayList<>();

        for(Object paramVal : params){
            String dynaBoundParamName = newDynaBoundSQLParamName();
            boundSqlParams.add(MybatisSqlUtils.formatBoundParameter(dynaBoundParamName));
            boundParams.put(dynaBoundParamName,paramVal);
        }

        boundSqlList.add(column+" in (" +MybatisSqlUtils.commaJoin(boundSqlParams)+") " );
    }

    /*
    * 字符串模糊搜索
    * */
    public void addColumnFuzzyStrLikeIntoWhere(String column,String object){
        String dynaBoundParamName = newDynaBoundSQLParamName();
        boundSqlList.add(column+" like CONCAT('%', " +MybatisSqlUtils.formatBoundParameter(dynaBoundParamName)+", '%') " );
        boundParams.put(dynaBoundParamName,object);
    }

    /*
    * 自定义sql
    * */
    public void addBoundSqlIntoWhere(
            String boundSQL,
            Map<String,Object> params
    ){
        boundSqlList.add(boundSQL);
        for(Map.Entry<String,Object> mapEntry : params.entrySet()){
            if(boundParams.containsKey(mapEntry.getKey())){
                throw new IllegalArgumentException("Duplicate key value:key["+mapEntry.getKey()+"]");
            }
            boundParams.put(mapEntry.getKey(),mapEntry.getValue());
        }
    }


    public String formatWhere(){
        return MybatisSqlUtils.wherePartJoin(boundSqlList);
    }
    public List<String> getBoundSqlList(){
        return boundSqlList;
    }
    public String[] getBoundSqlArray(){
        return boundSqlList.toArray(MybatisArrayUtils.EMPTY_STRING_ARRAY);
    }
    public Map<String,Object> getBoundParams(){
        return boundParams;
    }

}
