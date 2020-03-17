package com.econage.core.db.mybatis.mapper.provider;

import com.econage.core.db.mybatis.util.MybatisPreconditions;
import com.econage.core.db.mybatis.util.MybatisPrimitives;

import java.lang.reflect.Method;

class ProviderSqlSourceUtils {
    static Class<?>[] parseProviderMethodParameterTypes(Method providerMethod){
        MybatisPreconditions.checkNotNull(providerMethod,"providerMethod is null!");
        Class<?>[] providerMethodParameterTypes = providerMethod.getParameterTypes();
        for(int i=0,l=providerMethodParameterTypes.length;i<l;i++){
            Class<?> parameterType = providerMethodParameterTypes[i];
            if(parameterType.isPrimitive()){
                providerMethodParameterTypes[i] = MybatisPrimitives.wrap(parameterType);
            }
        }
        return providerMethodParameterTypes;
    }

}
