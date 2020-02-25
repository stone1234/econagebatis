package com.econage.core.db.mybatis.mapper.strengthen;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Primitives;

import java.lang.reflect.Method;

class ProviderSqlSourceUtils {
    static Class<?>[] parseProviderMethodParameterTypes(Method providerMethod){
        Preconditions.checkNotNull(providerMethod,"providerMethod is null!");
        Class<?>[] providerMethodParameterTypes = providerMethod.getParameterTypes();
        for(int i=0,l=providerMethodParameterTypes.length;i<l;i++){
            Class<?> parameterType = providerMethodParameterTypes[i];
            if(parameterType.isPrimitive()){
                providerMethodParameterTypes[i] = Primitives.wrap(parameterType);
            }
        }
        return providerMethodParameterTypes;
    }

}
