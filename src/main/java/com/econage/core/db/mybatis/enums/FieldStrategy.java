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
package com.econage.core.db.mybatis.enums;

/**
 * <p>
 * 选择性更新及插入，字段的判断逻辑。
 * </p>
 */
public enum FieldStrategy {
    IGNORED(0, "忽略判断"),//默认的选择性更新及插入，会默认使用不做空值判断
    NOT_NULL(1, "非 NULL 判断"),//默认的选择性更新及插入，会判断是否是null
    NOT_EMPTY(2, "非空判断");//对于字符串，默认的选择性更新及插入，会判断是否是null或者空白字符串

    /**
     * 主键
     */
    private final int key;

    /**
     * 描述
     */
    private final String desc;

    FieldStrategy(final int key, final String desc) {
        this.key = key;
        this.desc = desc;
    }

    public static FieldStrategy getFieldStrategy(int key) {
        FieldStrategy[] fss = FieldStrategy.values();
        for (FieldStrategy fs : fss) {
            if (fs.getKey() == key) {
                return fs;
            }
        }
        return FieldStrategy.NOT_NULL;
    }

    public int getKey() {
        return this.key;
    }

    public String getDesc() {
        return this.desc;
    }

}
