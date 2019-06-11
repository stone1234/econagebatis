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

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;
import java.util.Properties;

/**
 * <p>
 * 插件工具类
 * </p>
 *
 * @author TaoYu , hubin
 * @since 2017-06-20
 */
public final class MybatisPluginUtils {

    public static final String DELEGATE_BOUNDSQL_SQL = "delegate.boundSql.sql";
    public static final String DELEGATE_MAPPEDSTATEMENT = "delegate.mappedStatement";

    private MybatisPluginUtils() {
        // to do nothing
    }

    /**
     * <p>
     * 获取当前执行 MappedStatement
     * </p>
     *
     * @param metaObject 元对象
     * @return
     */
    public static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement) metaObject.getValue(DELEGATE_MAPPEDSTATEMENT);
    }

    /**
     * <p>
     * 获得真正的处理对象,可能多层代理.
     * </p>
     */
    public static Object realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return target;
    }

    /**
     * <p>
     * 根据 key 获取 Properties 的值
     * </p>
     */
    public static String getProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        return MybatisStringUtils.isEmpty(value) ? null : value;
    }
}
