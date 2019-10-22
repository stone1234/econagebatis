/**
 *    Copyright 2009-2019 the original author or authors.
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
package com.econage.core.db.mybatis.mapper.provider;

import com.econage.core.db.mybatis.adaptation.MybatisConfiguration;
import com.econage.core.db.mybatis.entity.TableInfo;
import com.econage.core.db.mybatis.util.MybatisMapUtils;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.LanguageDriver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class MybatisProviderSqlSource implements SqlSource {

  /*todo */
  private final MybatisConfiguration configuration;
  private final Class<?> providerType;
  private final LanguageDriver languageDriver;
  private final Method mapperMethod;
  private final Method providerMethod;
  private final String[] providerMethodArgumentNames;
  private final Class<?>[] providerMethodParameterTypes;
  //todo
  private MybatisProviderContext providerContextTpl;
  private final Integer providerContextIndex;

  /*
   * todo
   * */
  private static volatile Constructor<ProviderContext> PROVIDER_CONTEXT_CONSTRUCTOR;
  protected static ProviderContext createMybatisProviderContext(
          Class<?> mapperType,
          Method mapperMethod,
          String databaseId
  ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    if(PROVIDER_CONTEXT_CONSTRUCTOR ==null){
      synchronized (ProviderContext.class){
        if(PROVIDER_CONTEXT_CONSTRUCTOR ==null){
          Constructor<ProviderContext> providerContextConstructor = ProviderContext.class.getDeclaredConstructor(Class.class,Method.class,String.class);
          providerContextConstructor.setAccessible(true);
          PROVIDER_CONTEXT_CONSTRUCTOR = providerContextConstructor;
        }
      }
    }
    return PROVIDER_CONTEXT_CONSTRUCTOR.newInstance(mapperType, mapperMethod,databaseId);
  }

  /**
   * @since 3.5.3
   */
  public MybatisProviderSqlSource(
          /*todo */
          MybatisConfiguration configuration,
          Annotation provider,
          Class<?> mapperType,
          Method mapperMethod,
          //todo
          TableInfo tableInfo
  ) {
    String candidateProviderMethodName;
    Method candidateProviderMethod = null;
    try {
      this.configuration = configuration;
      this.mapperMethod = mapperMethod;
      Lang lang = mapperMethod == null ? null : mapperMethod.getAnnotation(Lang.class);
      this.languageDriver = configuration.getLanguageDriver(lang == null ? null : lang.value());
      this.providerType = getProviderType(provider, mapperMethod);
      candidateProviderMethodName = (String) provider.annotationType().getMethod("method").invoke(provider);

      /*
       * todo 原生的ProviderMethodResolver类，会查找providerType与mapperMethod同名的办法，但是需要ProviderContext类型参数
       *  ProviderContext类型无法被继承，构造函数权限为default
       * */
      if (candidateProviderMethodName.length() == 0 && ProviderMethodResolver.class.isAssignableFrom(this.providerType)) {
        candidateProviderMethod = ((ProviderMethodResolver) this.providerType.getDeclaredConstructor().newInstance())
            .resolveMethod(createMybatisProviderContext(mapperType,mapperMethod,configuration.getDatabaseId()));
      }
      if (candidateProviderMethod == null) {
        candidateProviderMethodName = candidateProviderMethodName.length() == 0 ? "provideSql" : candidateProviderMethodName;
        for (Method m : this.providerType.getMethods()) {
          if (candidateProviderMethodName.equals(m.getName()) && CharSequence.class.isAssignableFrom(m.getReturnType())) {
            if (candidateProviderMethod != null) {
              throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                  + candidateProviderMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                  + "'. Sql provider method can not overload.");
            }
            candidateProviderMethod = m;
          }
        }
      }
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
    if (candidateProviderMethod == null) {
      throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
          + candidateProviderMethodName + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
    }
    this.providerMethod = candidateProviderMethod;
    this.providerMethodArgumentNames = new ParamNameResolver(configuration, this.providerMethod).getNames();
    //todo 修正mybatis框架，处理的单个参数时，参数为原型类型导致错误的问题
    this.providerMethodParameterTypes = ProviderSqlSourceUtils.parseProviderMethodParameterTypes(providerMethod);
    //todo
    this.providerContextTpl = new MybatisProviderContext(
            configuration,
            mapperType,
            mapperMethod,
            configuration.getDatabaseId(),
            tableInfo
    );
    Integer candidateProviderContextIndex = null;
    for (int i = 0; i < this.providerMethodParameterTypes.length; i++) {
      Class<?> parameterType = this.providerMethodParameterTypes[i];
      /*
       * todo
       * */
      if (parameterType == MybatisProviderContext.class) {
        if (candidateProviderContextIndex != null) {
          throw new BuilderException("Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method ("
              + this.providerType.getName() + "." + providerMethod.getName()
              + "). ProviderContext can not define multiple in SqlProvider method argument.");
        }
        candidateProviderContextIndex = i;
      }
    }
    this.providerContextIndex = candidateProviderContextIndex;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    //todo 放了方便provider类中的方法，填充额外参数，每次调用时，复制一个MybatisProviderContext
    MybatisProviderContext providerContext = providerContextTpl.clone();
    SqlSource sqlSource = createSqlSource(parameterObject,providerContext);
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    //todo 填充providerContext中的additionalParam
    if(MybatisMapUtils.isNotEmpty(providerContext.getAdditionalParam())){
      for(Map.Entry<String,Object> entry : providerContext.getAdditionalParam().entrySet()){
        boundSql.setAdditionalParameter(entry.getKey(),entry.getValue());
      }
    }
    return boundSql;
  }

  private SqlSource createSqlSource(
          Object parameterObject,
          /*todo*/
          MybatisProviderContext providerContext
  ) {
    try {
      String sql;
      if (parameterObject instanceof Map) {
        //todo
        int bindParameterCount = providerMethodParameterTypes.length - (providerContextIndex == null ? 0 : 1);
        if (bindParameterCount == 1 &&
          (providerMethodParameterTypes[Integer.valueOf(0).equals(providerContextIndex) ? 1 : 0].isAssignableFrom(parameterObject.getClass()))) {
          //如果除context外，只有一个普通参数
          /*todo*/
          sql = invokeProviderMethod(extractProviderMethodArguments(providerContext,parameterObject));
        } else {
          //除context类型参数外，有多个参数，按照参数名称将map内容映射到对应的参数位置中
          //mybatis会将代理方法中的多个参数整理成map
          @SuppressWarnings("unchecked")
          Map<String, Object> params = (Map<String, Object>) parameterObject;
          /*todo*/
          sql = invokeProviderMethod(extractProviderMethodArguments(providerContext,params, providerMethodArgumentNames));
        }
      //单个参数或者无参场景
      } else if (providerMethodParameterTypes.length == 0) {
        sql = invokeProviderMethod();
      } else if (providerMethodParameterTypes.length == 1) {
        /*todo*/
        if (providerContextIndex == null) {
          sql = invokeProviderMethod(parameterObject);
        } else {
          sql = invokeProviderMethod(providerContext);
        }
      } else if (providerMethodParameterTypes.length == 2) {
        /*todo*/
        sql = invokeProviderMethod(extractProviderMethodArguments(providerContext,parameterObject));
      } else {
        throw new BuilderException("Cannot invoke SqlProvider method '" + providerMethod
          + "' with specify parameter '" + (parameterObject == null ? null : parameterObject.getClass())
          + "' because SqlProvider method arguments for '" + mapperMethod + "' is an invalid combination.");
      }
      Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
      return languageDriver.createSqlSource(configuration, sql, parameterType);
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error invoking SqlProvider method '" + providerMethod
          + "' with specify parameter '" + (parameterObject == null ? null : parameterObject.getClass()) + "'.  Cause: " + extractRootCause(e), e);
    }
  }

  private Throwable extractRootCause(Exception e) {
    Throwable cause = e;
    while(cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  private Object[] extractProviderMethodArguments(
          /*todo*/
          MybatisProviderContext providerContext,
          Object parameterObject
  ) {
    //todo
    if (providerContext != null) {
      Object[] args = new Object[2];
      args[providerContextIndex == 0 ? 1 : 0] = parameterObject;
      args[providerContextIndex] = providerContext;
      return args;
    } else {
      return new Object[] { parameterObject };
    }
  }

  private Object[] extractProviderMethodArguments(
          /*todo*/
          MybatisProviderContext providerContext,
          Map<String, Object> params,
          String[] argumentNames
  ) {
    Object[] args = new Object[argumentNames.length];
    for (int i = 0; i < args.length; i++) {
      if (providerContextIndex != null && providerContextIndex == i) {
        args[i] = providerContext;
      } else {
        args[i] = params.get(argumentNames[i]);
      }
    }
    return args;
  }

  private String invokeProviderMethod(Object... args) throws Exception {
    Object targetObject = null;
    if (!Modifier.isStatic(providerMethod.getModifiers())) {
      targetObject = providerType.getDeclaredConstructor().newInstance();
    }
    CharSequence sql = (CharSequence) providerMethod.invoke(targetObject, args);
    return sql != null ? sql.toString() : null;
  }

  private Class<?> getProviderType(Annotation providerAnnotation, Method mapperMethod)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> type = (Class<?>) providerAnnotation.annotationType().getMethod("type").invoke(providerAnnotation);
    Class<?> value = (Class<?>) providerAnnotation.annotationType().getMethod("value").invoke(providerAnnotation);
    if (value == void.class && type == void.class) {
      throw new BuilderException("Please specify either 'value' or 'type' attribute of @"
          + providerAnnotation.annotationType().getSimpleName()
          + " at the '" + mapperMethod.toString() + "'.");
    }
    if (value != void.class && type != void.class && value != type) {
      throw new BuilderException("Cannot specify different class on 'value' and 'type' attribute of @"
          + providerAnnotation.annotationType().getSimpleName()
          + " at the '" + mapperMethod.toString() + "'.");
    }
    return value == void.class ? type : value;
  }

}
