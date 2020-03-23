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
package com.flowyun.cornerstone.db.mybatis.annotations;

import com.flowyun.cornerstone.db.mybatis.enums.IdType;

import java.lang.annotation.*;

/**
 * <p>
 * 表主键标识
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface TableId {

    /**
     * <p>
     * 字段值（驼峰命名方式，该值可无）
     * </p>
     */
    String value() default "";

    /**
     * <p>
     * 主键ID
     * </p>
     * {@link IdType}
     */
    IdType type() default IdType.NONE;

}
