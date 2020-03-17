package com.econage.core.db.mybatis.util;

public class MybatisPreconditions {

    public static <T extends Object> T checkNotNull(
            T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static <T extends Object> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
