package com.econage.core.db.mybatis.util;

import java.util.Collection;

public class MybatisCollectionUtils {


    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

}
