/*
 * Copyright 2017-2018, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowyun.cornerstone.db.mybatis.uid.dbuid;

import com.flowyun.cornerstone.db.mybatis.uid.dbincrementer.IKeyGenerator;

/**
 * <p>
 * Oracle Key Sequence 生成器
 * oracle层面推荐用SYS_GUID函数处理主键
 * </p>
 */
public class OracleGuidGenerator implements IKeyGenerator {

    @Override
    public String executeSql(String incrementerName) {
       /* StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(incrementerName);
        sql.append(".NEXTVAL FROM DUAL");
        return sql.toString();*/
       return "SELECT SYS_GUID() FROM DUAL";
    }
}
