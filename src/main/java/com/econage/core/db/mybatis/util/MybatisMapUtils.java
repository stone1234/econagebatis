package com.econage.core.db.mybatis.util;

import java.util.Map;

public class MybatisMapUtils {


    public static boolean isEmpty(final Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(final Map<?,?> map) {
        return !isEmpty(map);
    }

}
