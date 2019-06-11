package com.econage.core.db.mybatis.util;

import com.econage.core.db.mybatis.enums.IEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class MybatisIEnumUtils {
    /**
     * <p>
     * 值映射为枚举
     * </p>
     *
     * @param enumClass 枚举类
     * @param value     枚举值
     * @return
     */
    public static IEnum valueOf(Class<?> enumClass, Object value) {
        if(!enumClass.isEnum()){
            return null;
        }

        for (Object e : enumClass.getEnumConstants()) {
            if(!(e instanceof IEnum)){
                continue;
            }

            IEnum iEnum = (IEnum)e;
            if (iEnum.getValue() == value||Objects.equals(iEnum.getValue(),value)) {
                // 基本类型
                return iEnum;
            } else if (value instanceof Number) {
                if (iEnum.getValue() instanceof Number &&
                        ((Number) value).doubleValue() == ((Number) iEnum.getValue()).doubleValue()) {
                    return iEnum;
                }
            } else if(StringUtils.equals(
                    String.valueOf(value),
                    String.valueOf(iEnum.getValue())
            )) {
                return iEnum;
            }
        }
        return null;
    }

}
