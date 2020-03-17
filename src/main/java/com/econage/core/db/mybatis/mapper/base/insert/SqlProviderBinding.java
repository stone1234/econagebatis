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
package com.econage.core.db.mybatis.mapper.base.insert;

import java.util.HashMap;
import java.util.Map;

public class SqlProviderBinding {
    public final String sql;
    public final Map<String,Object> additionalParameter;

    public static SqlProviderBinding of(String sql) {
        return new SqlProviderBinding(sql, null);
    }
    public static SqlProviderBinding of(String sql, Map<String,Object> additionalParameter) {
        return new SqlProviderBinding(sql, additionalParameter);
    }

    public SqlProviderBinding(String sql, Map<String, Object> additionalParameter) {
        this.sql = sql;
        if(additionalParameter==null){
            additionalParameter = new HashMap<>();
        }
        this.additionalParameter = additionalParameter;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getAdditionalParameter() {
        return additionalParameter;
    }

}
