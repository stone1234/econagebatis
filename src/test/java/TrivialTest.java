import com.econage.core.db.mybatis.dyna.DynaClass;
import com.econage.core.db.mybatis.mapper.dyna.DynaBeanMapper;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class TrivialTest {
    @Test
    public void TypeParameterResolverTest() throws NoSuchMethodException {
        Class<?> type = DynaBeanMapper.class;
        Method method = type.getMethod("selectListByIds", DynaClass.class, Collection.class);

        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);

        System.out.println(resolvedReturnType instanceof ParameterizedType);
    }

}
