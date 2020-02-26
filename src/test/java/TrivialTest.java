import com.econage.core.db.mybatis.mapper.dyna.entity.DynaClass;
import com.econage.core.db.mybatis.mapper.DynaBeanMapper;
import entity.TestMapper;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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

    @Test
    public void mapperTest(){
        SqlSessionFactory sqlSessionFactory = SessionFactoryHolder.getInstance().getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        TestMapper mapper = sqlSession.getMapper(TestMapper.class);

        /*TestEntity entity = new TestEntity();
        entity.setFk("aa");
        entity.setText1("1");
        entity.setText2("2");

        mapper.insert(entity);

        entity.setId(null);
        entity.setText2(null);
        mapper.insert(entity);*/

        //mapper.deleteById("1187319930285559810");
        //mapper.deleteByIds(Collections.singletonList("1187319930684018690"));
        //mapper.deleteByFk("aa");
        /*TestWhereLogic testWhereLogic = new TestWhereLogic();
        testWhereLogic.setText1("1");*/
        //mapper.deleteByWhereLogic(testWhereLogic);

        /*TestEntity entity = new TestEntity();
        entity.setId("1187323607213735937");
        entity.setText2("ddddd");
        entity.setText1("aaaaaa");
        mapper.updatePartialColumnById(entity, Arrays.asList("text1"));*/

        /*TestEntity entity = new TestEntity();
        entity.setText1("eeeeeeee");
        TestWhereLogic whereLogic = new TestWhereLogic();
        whereLogic.setText1("aaaaaa");
        mapper.updateBatchByWhereLogic(entity,whereLogic);*/

        /*mapper.selectById("1187323606815277058");
        mapper.selectListByIds(Collections.singletonList("1187323606815277058"));
        mapper.selectListByPage(null);*/
        TestWhereLogic whereLogic = new TestWhereLogic();
        whereLogic.setText1("eeeeeeee");
        mapper.selectCountByWhereLogic(whereLogic);

        sqlSession.commit(true);
    }

}
