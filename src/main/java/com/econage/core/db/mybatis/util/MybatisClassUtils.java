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

import com.econage.core.db.mybatis.MybatisException;
import com.econage.core.db.mybatis.MybatisPackageInfo;
import com.econage.core.db.mybatis.dyna.adaptation.DynaBeanExecutor;
import com.econage.core.db.mybatis.mapper.BaseMapper;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.Reflection;
import org.apache.commons.lang3.ClassUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * ClassUtils
 */
public class MybatisClassUtils {

    private static final Log logger = LogFactory.getLog(MybatisClassUtils.class);

    public static Class<?> extractModelClass(Class<?> mapperClass) {
        if (mapperClass == BaseMapper.class) {
            logger.warn(" Current Class is BaseMapper ");
            return null;
        } else {
            Type[] types = mapperClass.getGenericInterfaces();
            ParameterizedType target = null;
            for (Type type : types) {
                if (type instanceof ParameterizedType && BaseMapper.class.isAssignableFrom(mapperClass)) {
                    target = (ParameterizedType) type;
                    break;
                }
            }
            return target == null ? null : (Class<?>) target.getActualTypeArguments()[0];
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
        Preconditions.checkNotNull(object,"Error: Instance must not be null");
        return getUserClass(object.getClass());
    }


    public static Class<?> parseBaseMapperInterfaceInService(Class<?> mapperClsInService){
        if(mapperClsInService==null||mapperClsInService==Object.class){
            return null;
        }

        List<Class<?>> allInterfaces =  ClassUtils.getAllInterfaces(mapperClsInService);
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

    private static final Set<Class<?>> ALL_PRIMITIVE_WRAPPER_TYPES = Primitives.allWrapperTypes();
    public static boolean isPrimitivesWrapperType(Class<?> clazz){
        return ALL_PRIMITIVE_WRAPPER_TYPES.contains(clazz);
    }
    private static final Set<Class<?>> ALL_PRIMITIVE_TYPES = Primitives.allPrimitiveTypes();
    public static boolean isPrimitivesType(Class<?> clazz){
        return ALL_PRIMITIVE_TYPES.contains(clazz);
    }

    public static final String[] EXCLUDE_CLAZZ_PREFIX_4_MODEL_PARSE_STATIC_ARRAY = {
            "java",
            "javax",
            "jdk",
            Reflection.getPackageName(MybatisPackageInfo.class)
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

    private static final Field cachingExecutorDelegate;
    static{
        Field cachingExecutorDelegate1;
        try {
            cachingExecutorDelegate1 = CachingExecutor.class.getField("delegate");
            cachingExecutorDelegate1.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            cachingExecutorDelegate1 = null;
        }
        cachingExecutorDelegate = cachingExecutorDelegate1;
    }

    //抽取executor包装器代理类
    public static Executor extractExecutor(Executor executor){
        if(executor instanceof BaseExecutor){
            return executor;
        }else if(executor instanceof DynaBeanExecutor){
            return ((DynaBeanExecutor)executor).getDelegate();
        }else if(executor instanceof CachingExecutor){
            try {
                return extractExecutor((Executor) cachingExecutorDelegate.get(executor));
            } catch (IllegalAccessException e) {
                throw new MybatisException("should never happen!");
            }
        }else{
            throw new MybatisException("unknown executor wrapper,should never happen,executor class:["+executor.getClass()+"]");
        }
    }

}
