import com.flowyun.cornerstone.db.mybatis.mapper.DynaBeanMapper;
import com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity.DynaClass;
import com.flowyun.cornerstone.db.mybatis.pagination.Pagination;
import entity.TestMapper;
import entity.TestShardingMapper;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class TrivialTest {
    @Test
    public void TypeParameterResolverTest() throws NoSuchMethodException {
        Class<?> type = DynaBeanMapper.class;
        Method method = type.getMethod("selectListByIds", DynaClass.class, Collection.class);

        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);

        System.out.println(resolvedReturnType instanceof ParameterizedType);
    }


    @Test
    public void mapper2Test() {
        SqlSessionFactory sqlSessionFactory = SessionFactoryHolder.getInstance().getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();


    }
    @Test
    public void mapperTest2() {
        SqlSessionFactory sqlSessionFactory = SessionFactoryHolder.getInstance().getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        TestShardingMapper mapper = sqlSession.getMapper(TestShardingMapper.class);
        String rnTab = "test2";

        /*TestEntity entity = new TestEntity();
        entity.setFk("aa");
        entity.setText1("1");
        entity.setText2(null);

        mapper.insert(rnTab,entity);

        entity.setId(null);
        entity.setText2(null);
        mapper.insertAllColumn(rnTab,entity);*/

       /* mapper.deleteById(rnTab,"1187319930285559810");
        mapper.deleteByIds(rnTab,Collections.singletonList("1187319930684018690"));
        mapper.deleteByFk(rnTab,"aa");
        TestWhereLogic testWhereLogic = new TestWhereLogic();
        testWhereLogic.setText1("1");
        mapper.deleteByWhereLogic(rnTab,testWhereLogic);*/

        /*TestEntity entity = new TestEntity();
        entity.setText1("eeeeeeee");
        TestWhereLogic whereLogic = new TestWhereLogic();
        whereLogic.setText1("aaaaaa");
        mapper.updateBatchByWhereLogic(rnTab,entity,whereLogic);*/

        sqlSession.commit(true);
    }
    @Test
    public void mapperTest(){
        SqlSessionFactory sqlSessionFactory = SessionFactoryHolder.getInstance().getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        TestMapper mapper = sqlSession.getMapper(TestMapper.class);

        /*TestEntity entity = new TestEntity();
        entity.setFk("aa");
        entity.setText1("1");
        entity.setText2(null);
        entity.setText3(Locale.CHINA);
        Map<String,Object> map = new HashMap<>();
        map.put(ENTITY_PARAM_NAME,entity);


        sqlSession.insert("entity.TestMapper.insert",map);*/

        /*

        mapper.insert(entity);

        entity.setId(null);
        entity.setText2(null);
        entity.setText3(null);
        mapper.insertAllColumn(entity);*/
/*
        mapper.deleteById("1187319930285559810");
        mapper.deleteByIds(Collections.singletonList("1187319930684018690"));
        mapper.deleteByFk("aa");
        TestWhereLogic testWhereLogic = new TestWhereLogic();
        testWhereLogic.setText1("1");
        mapper.deleteByWhereLogic(testWhereLogic);*/

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
        mapper.selectListByFk(Collections.singletonList("1187323606815277058"),null);

        mapper.selectListByPage(null);*/
        List rs = mapper.selectListByPage(Pagination.newPaginationWithPageRows(1,30));
        /*TestWhereLogic whereLogic = new TestWhereLogic();
        whereLogic.setText1("eeeeeeee");
        mapper.selectCountByWhereLogic(whereLogic);*/

        sqlSession.commit(true);
    }

}
