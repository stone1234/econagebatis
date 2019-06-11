package com.econage.core.db.mybatis.wherelogic;

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.adaptation.MybatisGlobalAssistant;
import com.econage.core.db.mybatis.annotations.WhereLogic;
import com.econage.core.db.mybatis.annotations.WhereLogicField;
import com.econage.core.db.mybatis.util.*;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.util.*;

public class MybatisWhereLogicHelper {
    private static final Log logger = LogFactory.getLog(MybatisWhereLogicHelper.class);


    public static WhereLogicInfo parseWhereLogicInfo(
            MybatisGlobalAssistant globalAssistant,
            Class<?> whereLogicCls
    ){
        /*
        * 如果是原型，或者string类，或者不带注解，则认为不是表单对象
        * */
        if(whereLogicCls==null
         ||Primitives.allPrimitiveTypes().contains(whereLogicCls)
         ||Primitives.isWrapperType(whereLogicCls)
         ||whereLogicCls==String.class
         ||whereLogicCls.getAnnotation(WhereLogic.class)==null
        ){
            return null;
        }

        WhereLogicInfo whereLogicInfo = new WhereLogicInfo();
        whereLogicInfo.setClassName(whereLogicCls.getName());


        for(Field field : getAllFields(globalAssistant,whereLogicCls) ){
            try {
                whereLogicInfo.putSearchFieldInfo(parseField(globalAssistant,field));
            }  catch (InstantiationException|IllegalAccessException e) {
                throw new MybatisException(e);
            }
        }
        return whereLogicInfo;
    }

    public static List<String> parseWhereLogic(
            MybatisGlobalAssistant globalAssistant,
            Object whereLogicObj,
            Map<String,Object> additionMap
    ){
        if(whereLogicObj==null){
            return Collections.EMPTY_LIST;
        }
        WhereLogicInfo whereLogicInfo = globalAssistant.saveAndGetWhereLogic(whereLogicObj.getClass());
        if(whereLogicInfo ==null){
            return Collections.EMPTY_LIST;
        }

        MetaObject whereLogicMetaObject = globalAssistant.getConfiguration().newMetaObject(whereLogicObj);

        List<String> wherePart = Lists.newArrayListWithCapacity(whereLogicInfo.getFieldInfos().size());

        whereLogicInfo.getFieldInfos().forEach(whereLogicFieldInfo -> {
            String property = whereLogicFieldInfo.getProperty();
            Object propertyVal = whereLogicMetaObject.getValue(property);
            if(propertyVal==null){
                return;
            }

            //如果有解析器，则其他解析参数忽略
            if(whereLogicFieldInfo.getWhereLogicParser()!=null){
                wherePart.add(
                        whereLogicFieldInfo
                                .getWhereLogicParser()
                                .parseWhereLogic(WhereLogicContext.newContext(additionMap,whereLogicObj))
                );
                return;
            }

            if (whereLogicFieldInfo.isPrimitiveType()) {
                //原型0值判断需要特殊处理
                if (whereLogicFieldInfo.isUsePrimitiveZero()) {
                    //如果确定使用原型0值
                    wherePart.add(whereLogicFieldInfo.getWhereLogic());
                } else if (!isZero(propertyVal)) {
                    //如果不是0值
                    wherePart.add(whereLogicFieldInfo.getWhereLogic());
                }
                return;
            }

            if(whereLogicFieldInfo.isCollectionType()||whereLogicFieldInfo.isArrayType()){

                Collection<?> collectionVal ;
                if(whereLogicFieldInfo.isArrayType()){
                    //数组类型，需要额外转换成集合，方便处理
                    collectionVal= convert2Collection( propertyVal ) ;
                }else{
                    collectionVal = (Collection)propertyVal;
                }
                if(MybatisCollectionUtils.isEmpty(collectionVal)){
                    return;
                }

                String collectionReplaceHolder = MybatisSqlUtils.formatCollection2ParameterMappings(property, collectionVal, additionMap);

                wherePart.add(StringUtils.replace(
                        whereLogicFieldInfo.getWhereLogic(),
                        MybatisStringUtils.WHERE_LOGIC_COLLECTION_REPLACE,
                        collectionReplaceHolder
                ));
                return;
            }

            //普通类型或者原型包装类，如果非空，则放入谓语语句
            wherePart.add(whereLogicFieldInfo.getWhereLogic());
        } );

        return wherePart;
    }

    /* 是否对应原型里的0值*/
    private static Character ZERO_CHAR = Character.valueOf((char)0);
    private static boolean isZero(Object propertyVal){
        return Objects.equals(NumberUtils.LONG_ZERO,propertyVal)
                ||Objects.equals(NumberUtils.INTEGER_ZERO,propertyVal)
                ||Objects.equals(NumberUtils.SHORT_ZERO,propertyVal)
                ||Objects.equals(NumberUtils.BYTE_ZERO,propertyVal)
                ||Objects.equals(NumberUtils.FLOAT_ZERO,propertyVal)
                ||Objects.equals(NumberUtils.DOUBLE_ZERO,propertyVal)
                ||Objects.equals(Boolean.FALSE,propertyVal)
                ||Objects.equals(ZERO_CHAR,propertyVal);
    }

    private static Collection<?> convert2Collection(Object obj){
        if(obj==null){
            return Collections.EMPTY_LIST;
        }

        Class<?> objCls = obj.getClass();
        if(objCls==long[].class){
            return Arrays.asList( ArrayUtils.toObject( (long[])obj ));
        }else if(objCls==int[].class){
            return Arrays.asList( ArrayUtils.toObject( (int[])obj ));
        }else if(objCls==short[].class){
            return Arrays.asList( ArrayUtils.toObject( (short[])obj ));
        }else if(objCls==byte[].class){
            return Arrays.asList( ArrayUtils.toObject( (byte[])obj ));
        }else if(objCls==float[].class){
            return Arrays.asList( ArrayUtils.toObject( (float[])obj ));
        }else if(objCls==double[].class){
            return Arrays.asList( ArrayUtils.toObject( (double[])obj ));
        }else if(objCls==boolean[].class){
            return Arrays.asList( ArrayUtils.toObject( (boolean[])obj ));
        }else if(objCls==char[].class){
            return Arrays.asList( ArrayUtils.toObject( (char[])obj ));
        }else{
            throw new IllegalArgumentException("Not a array object");
        }
    }


    /*private static Object getProperty(Object obj,String property){
        try {
            return PropertyUtils.getProperty(obj,property);
        } catch (Exception e) {
            logger.error("error in parse property",e);
        }
        return null;
    }*/

    private static WhereLogicFieldInfo parseField(MybatisGlobalAssistant globalAssistant, Field field) throws IllegalAccessException, InstantiationException {
        WhereLogicFieldInfo whereLogicFieldInfo = new WhereLogicFieldInfo();
        whereLogicFieldInfo.setType(field.getType());
        whereLogicFieldInfo.setProperty(field.getName());

        //若干类型判断
        if(Collection.class.isAssignableFrom(field.getType())){
            whereLogicFieldInfo.setCollectionType(true);
        }else if(field.getType().isArray()){
            whereLogicFieldInfo.setArrayType(true);
        }else {
            whereLogicFieldInfo.setPrimitiveType(Primitives.allPrimitiveTypes().contains(field.getType()));
        }

        WhereLogicField whereLogicField = field.getAnnotation(WhereLogicField.class);
        if(whereLogicField !=null){
            //java中几类原型数据，默认是0值。0值很多时候数据库会有业务逻辑，此处通过注解控制，是否使用0
            whereLogicFieldInfo.setUsePrimitiveZero(whereLogicField.usePrimitiveZero());
        }
        if(whereLogicField !=null&& StringUtils.isNotEmpty(whereLogicField.column())){
            whereLogicFieldInfo.setColumn(whereLogicField.column());
        }else{
            whereLogicFieldInfo.setColumn(globalAssistant.formatColumn(field.getName()));
        }


        /*
        * 整理where部分语句，如果是集合，则需要使用特殊替换另外处理，如果注解有自己的解析语句，则使用注解的信息
        * */
        if(whereLogicField !=null&& StringUtils.isNotEmpty(whereLogicField.wherePart())){
            whereLogicFieldInfo.setWhereLogic(whereLogicField.wherePart());
        }else{
            if(whereLogicFieldInfo.isCollectionType()||whereLogicFieldInfo.isArrayType()){
                whereLogicFieldInfo.setWhereLogic(whereLogicFieldInfo.getColumn()+" in ( "+ MybatisStringUtils.WHERE_LOGIC_COLLECTION_REPLACE+" )");
            }else{
                whereLogicFieldInfo.setWhereLogic(whereLogicFieldInfo.getColumn()+"=#{"+ whereLogicFieldInfo.getProperty()+"}");
            }
        }

        if(whereLogicField!=null&&whereLogicField.parser()!=WhereLogicParser.class){
            whereLogicFieldInfo.setWhereLogicParser(whereLogicField.parser().newInstance());
        }

        return whereLogicFieldInfo;
    }


    /*
    * 处理有效的并且mybatis框架能处理的java类型
    * */
    private static List<Field> getAllFields(MybatisGlobalAssistant globalAssistant,Class<?> cls) {
        TypeHandlerRegistry typeHandlerRegistry = globalAssistant.getConfiguration().getTypeHandlerRegistry();
        List<Field> fieldList = MybatisReflectionKit.getFieldList(MybatisClassUtils.getUserClass(cls));
        if (MybatisCollectionUtils.isNotEmpty(fieldList)) {
            Iterator<Field> iterator = fieldList.iterator();
            while (iterator.hasNext()) {
                Field field = iterator.next();
                WhereLogicField whereLogicField = field.getAnnotation(WhereLogicField.class);
                Class<?> fileCls = field.getType();
                if (whereLogicField != null && !whereLogicField.enable()) {
                    /* 过滤注解非表字段属性 */
                    iterator.remove();
                }else if(Collection.class.isAssignableFrom(fileCls)){
                    /*
                     * 如果是集合类，则认为可以处理
                     * */
                    continue;
                }else if(fileCls.isArray()){
                    /*
                    * 如果是数组类型，则提取组件信息
                    * */
                    fileCls = fileCls.getComponentType();
                    if(!typeHandlerRegistry.hasTypeHandler(fileCls)){
                        iterator.remove();
                    }
                }else if(!typeHandlerRegistry.hasTypeHandler(fileCls)){
                    //mybatis框架没有转换逻辑的
                    iterator.remove();
                }
            }
        }
        return fieldList;
    }




}
