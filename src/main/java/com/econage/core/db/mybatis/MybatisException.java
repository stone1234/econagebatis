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
package com.econage.core.db.mybatis;

/**
 * <p>
 * 异常类
 * </p>
 */
public class MybatisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MybatisException() {
        super();
    }
    public MybatisException(String message) {
        super(message);
    }

    public MybatisException(Throwable throwable) {
        super(throwable);
    }

    public MybatisException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
