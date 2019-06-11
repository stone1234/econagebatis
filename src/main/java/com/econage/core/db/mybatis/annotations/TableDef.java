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
package com.econage.core.db.mybatis.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * 数据库表相关
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface TableDef {

    /**
     * <p>
     * 实体对应的表名
     * </p>
     */
    String value() default "";

    /**
     * <p>
     * 实体映射结果集
     * </p>
     */
    String defaultSelectResultMap() default "";

}
