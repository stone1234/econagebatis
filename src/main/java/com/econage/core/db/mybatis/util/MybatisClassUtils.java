/**
 *    Copyright 2017-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.econage.core.db.mybatis.util;

import com.econage.core.db.mybatis.MybatisPackageInfo;
import com.econage.core.db.mybatis.mapper.BaseMapper;
import com.econage.core.db.mybatis.mapper.ShardingMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * ClassUtils
 */
public class MybatisClassUtils {

    private static final Log logger = LogFactory.getLog(MybatisClassUtils.class);

    public static boolean isTopMapperInterface(Class<?> clazz){
        return BaseMapper.class==clazz||ShardingMapper.class==clazz;
    }
    public static boolean isAssignableFromTopMapperInterface(Class<?> clazz){
        return BaseMapper.class.isAssignableFrom(clazz)
                ||ShardingMapper.class.isAssignableFrom(clazz);
    }

    public static Class<?> extractModelClass(Class<?> mapperClass) {
        if (isTopMapperInterface(mapperClass)) {
            logger.warn(" Current Class is BaseMapper or ShardingMapper");
            return null;
        }else if(isAssignableFromTopMapperInterface(mapperClass)){
            Type[] types = mapperClass.getGenericInterfaces();
            ParameterizedType target = null;
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    target = (ParameterizedType) type;
                    break;
                }
            }
            return target == null ? null : (Class<?>) target.getActualTypeArguments()[0];
        } else {
            return null;
        }
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz
     * @return
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                String interfaceName = cls.getName();
                if (interfaceName.equals("net.sf.cglib.proxy.Factory") //cglib
                        || interfaceName.equals("org.springframework.cglib.proxy.Factory")
                        || interfaceName.equals("javassist.util.proxy.ProxyObject") //javassist
                        || interfaceName.equals("org.apache.ibatis.javassist.util.proxy.ProxyObject")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前对象的class
     *
     * @param clazz
     * @return
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        return isProxy(clazz) ? clazz.getSuperclass() : clazz;
    }

    /**
     * 获取当前对象的class
     *
     * @param object
     * @return
     */
    public static Class<?> getUserClass(Object object) {
        MybatisPreconditions.checkNotNull(object,"Error: Instance must not be null");
        return getUserClass(object.getClass());
    }


    public static Class<?> parseBaseMapperInterfaceInService(Class<?> mapperClsInService){
        if(mapperClsInService==null||mapperClsInService==Object.class){
            return null;
        }

        List<Class<?>> allInterfaces =  getAllInterfaces(mapperClsInService);
        for(Class<?> singleInterface : allInterfaces){
            /*
             * 接口继承了BaseMapper
             * */
            Class<?>[] parentInterfaceArray = singleInterface.getInterfaces();
            for(Class<?> i : parentInterfaceArray){
                if(i == BaseMapper.class){
                    return singleInterface;
                }
            }
            /*
            * 接口带了Mapper注解
            * */
            if(singleInterface.getAnnotation(Mapper.class)!=null){
                return singleInterface;
            }
        }
        return null;
    }


    public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<>(interfacesFound);
    }

    /**
     * Get the interfaces for the specified class.
     *
     * @param cls  the class to look up, may be {@code null}
     * @param interfacesFound the {@code Set} of interfaces for the class
     */
    private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            final Class<?>[] interfaces = cls.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    private static final Set<Class<?>> ALL_PRIMITIVE_WRAPPER_TYPES = MybatisPrimitives.allWrapperTypes();
    public static boolean isPrimitivesWrapperType(Class<?> clazz){
        return ALL_PRIMITIVE_WRAPPER_TYPES.contains(clazz);
    }
    private static final Set<Class<?>> ALL_PRIMITIVE_TYPES = MybatisPrimitives.allPrimitiveTypes();
    public static boolean isPrimitivesType(Class<?> clazz){
        return ALL_PRIMITIVE_TYPES.contains(clazz);
    }

    public static final String[] EXCLUDE_CLAZZ_PREFIX_4_MODEL_PARSE_STATIC_ARRAY = {
            "java",
            "javax",
            "jdk",
            getPackageName(MybatisPackageInfo.class)
    };
    //是否排除在扫描外的类
    public static boolean excludeClazzPrefix4ModelParseStatic(Class<?> clazz){
        String modelName = clazz.getName();
        for(String excludeClazzPrefix: EXCLUDE_CLAZZ_PREFIX_4_MODEL_PARSE_STATIC_ARRAY){
            if(modelName.startsWith(excludeClazzPrefix)){
                return true;
            }
        }
        return isPrimitivesType(clazz)||isPrimitivesWrapperType(clazz);
    }


    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }
    public static String getPackageName(String classFullName) {
        int lastDot = classFullName.lastIndexOf('.');
        return (lastDot < 0) ? "" : classFullName.substring(0, lastDot);
    }
}
