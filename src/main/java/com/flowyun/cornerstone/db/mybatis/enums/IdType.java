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
package com.flowyun.cornerstone.db.mybatis.enums;

public enum IdType {
    AUTO("数据库ID自增"), INPUT("用户输入ID"),

    /* 以下2种类型、只有当插入对象ID 为空，才自动填充。 */
    ID_WORKER("全局唯一ID"), UUID("全局唯一ID"),

    //SEQUENCE(""),

    /*此时，如果KeySequence非空，则使用KeySequence，如果KeySequence为空，则以全局配置类为准*/
    NONE("该类型为未设置主键类型");
    /**
     * 描述
     */
    private final String desc;

    IdType(final String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
