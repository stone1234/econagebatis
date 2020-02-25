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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MybatisSqlUtils {
    public static final String STATIC_FALSE_WHERE_SQL = " 1<>1 ",
                               STATIC_TRUE_WHERE_SQL  = " 1=1 ";

    public static final String NEW_VERSION_STAMP_SUFFIX = "_new_stamp__";
    public static final String CURR_VERSION_STAMP_SUFFIX = "_curr_stamp__";


    public static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    public static String formatCollection2ParameterMappings(
            String item,
            Collection<?> typeParams,
            Map<String,Object> additionalMap
    ){
        return formatCollection2ParameterMappings(
                StringUtils.EMPTY,StringUtils.EMPTY,item,
                typeParams,
                additionalMap
        );
    }

    //解析结合类型的参数，转换为合适的占位符字符串，并存入map映射
    public static String formatCollection2ParameterMappings(
            String open, String close,String item,
            Collection<?> typeParams,
            Map<String,Object> additionalMap
    ){
        Preconditions.checkNotNull(item,"item is null!");
        if(additionalMap==null){
            throw new NullPointerException(" additional map is null!");
        }

        List<String> parameterTokenList = typeParams.stream().map(new Function<Object, String>() {
            int count = 0;
            @Override
            public String apply(Object input) {
                String parameterToken = item+ MybatisStringUtils.UNDERLINE_STR+count;
                count++;
                additionalMap.put(parameterToken,input);
                return "#{"+parameterToken+"}";
            }
        }).collect(Collectors.toList());


        StringBuilder tokens = new StringBuilder();
        if(MybatisStringUtils.isNotEmpty(open)){
            tokens.append(open);
        }
        tokens.append(COMMA_JOINER.join(parameterTokenList));
        if(MybatisStringUtils.isNotEmpty(close)){
            tokens.append(close);
        }
        return tokens.toString();
    }

    public static String commaJoin(Collection<String> stringCollection){
        return COMMA_JOINER.join(stringCollection);
    }
    public static String commaJoin(String... stringCollection){
        return COMMA_JOINER.join(stringCollection);
    }


    protected static final Joiner WHERE_PART_JOINER = Joiner.on(" and ").skipNulls();
    public static String wherePartJoin(Collection<String> stringCollection){
        return " "+WHERE_PART_JOINER.join(stringCollection)+" ";
    }
    public static String wherePartJoin(String prefix,Collection<String> stringCollection){
        return (prefix!=null?prefix:StringUtils.EMPTY)+" "+WHERE_PART_JOINER.join(stringCollection)+" ";
    }
    /*public static String wherePartJoin(String... stringCollection){
        return " "+WHERE_PART_JOINER.join(stringCollection)+" ";
    }*/
    /*public static String wherePartJoin(String prefix,String... stringCollection){
        return (prefix!=null?prefix:StringUtils.EMPTY)+" "+WHERE_PART_JOINER.join(stringCollection)+" ";
    }*/

    /**
     * <p>
     * 判断数据库操作是否成功
     * </p>
     *
     * @param result 数据库操作返回影响条数
     * @return boolean
     */
    public static boolean retBool(Integer result) {
        return null != result && result >= 1;
    }

    /**
     * <p>
     * 删除不存在的逻辑上属于成功
     * </p>
     *
     * @param result 数据库操作返回影响条数
     * @return boolean
     */
    public static boolean delBool(Integer result) {
        return null != result && result >= 0;
    }

    /**
     * <p>
     * 返回SelectCount执行结果
     * </p>
     *
     * @param result
     * @return int
     */
    public static int retCount(Integer result) {
        return (null == result) ? 0 : result;
    }


    public static String formatBoundParameter(String propertyNameInBoundSQL){
        return "#{"+propertyNameInBoundSQL+"}";
    }
}
