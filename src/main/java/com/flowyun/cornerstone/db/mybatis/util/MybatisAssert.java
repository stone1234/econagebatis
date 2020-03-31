package com.flowyun.cornerstone.db.mybatis.util;


import java.util.Collection;

public class MybatisAssert {

    public static void notNull( Object object, String message) {
        if (object==null) {
            throw new IllegalArgumentException(message);
        }
    }
    public static void isNull( Object object, String message) {
        if (object!=null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty( String object, String message) {
        if (MybatisStringUtils.isEmpty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty( String object, String message) {
        if (MybatisStringUtils.isNotEmpty(object)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty( Collection<?> collection, String message) {
        if (MybatisCollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty( Collection<?> collection, String message) {
        if (MybatisCollectionUtils.isNotEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

}
