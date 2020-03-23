package com.flowyun.cornerstone.db.mybatis.mapper.dyna.adaptation;

import com.flowyun.cornerstone.db.mybatis.MybatisException;
import com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity.DynaBean;
import com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity.DynaClass;
import com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity.DynaColumn;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
* 动态bean结果集解析
* 仅支持普通语句，不支持存储过程
* 不支持指针，如果遇到大数据，使用分页查询
* */
public class DynaBeanResultSetHandler implements ResultSetHandler {

    private final Executor executor;
    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final ParameterHandler parameterHandler;
    private final ResultHandler<?> resultHandler;
    private final BoundSql boundSql;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;
    private final ReflectorFactory reflectorFactory;
    private final DynaClass dynaClass;

    public DynaBeanResultSetHandler(
            Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql,
            RowBounds rowBounds, DynaClass dynaClass) {
        this.executor = executor;
        this.configuration = mappedStatement.getConfiguration();
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;
        this.parameterHandler = parameterHandler;
        this.boundSql = boundSql;
        this.dynaClass = dynaClass;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();
        this.reflectorFactory = configuration.getReflectorFactory();
        this.resultHandler = resultHandler;
    }



    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

        final List<Object> multipleResults = new ArrayList<>();

        ResultSetWrapper rsw = getFirstResultSet(stmt);
        validateResultMapsCount(rsw, mappedStatement.getResultMaps().size());
        if (rsw != null ) {
            handleResultSet(rsw, multipleResults);
        }

        return collapseSingleResultList(multipleResults);
    }

    @SuppressWarnings("unchecked")
    private List<Object> collapseSingleResultList(List<Object> multipleResults) {
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }

    private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
        ResultSet rs = stmt.getResultSet();
        while (rs == null) {
            // move forward to get the first resultset in case the driver
            // doesn't return the resultset as the first result (HSQLDB 2.1)
            if (stmt.getMoreResults()) {
                rs = stmt.getResultSet();
            } else {
                if (stmt.getUpdateCount() == -1) {
                    // no more results. Must be no resultset
                    break;
                }
            }
        }
        return rs != null ? new ResultSetWrapper(rs, configuration) : null;
    }
    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
    private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
        if (rsw != null && resultMapCount < 1) {
            throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
                    + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
        }
    }

    private void handleResultSet(ResultSetWrapper rsw, List<Object> multipleResults) throws SQLException {
        try {
            if (resultHandler == null) {
                DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
                handleRowValues(rsw, defaultResultHandler);
                multipleResults.add(defaultResultHandler.getResultList());
            } else {
                handleRowValues(rsw, resultHandler);
            }
        } finally {
            // issue #228 (close resultsets)
            closeResultSet(rsw.getResultSet());
        }
    }

    public void handleRowValues(ResultSetWrapper rsw, ResultHandler<?> resultHandler) throws SQLException {
        DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
        ResultSet resultSet = rsw.getResultSet();
        while (!resultContext.isStopped() && !resultSet.isClosed() && resultSet.next()) {
            resultContext.nextResultObject(getRowValue(rsw));
            ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
        }
    }

    private DynaBean getRowValue(ResultSetWrapper rsw) throws SQLException {
        DynaBean dynaBean = new DynaBean(dynaClass);
        for(DynaColumn dynaColumn :  dynaClass.getDynaColumns()){
            String columnName = dynaColumn.getName();
            TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(dynaColumn.getType(),rsw.getJdbcType(columnName));
            if(typeHandler!=null){
                dynaBean.set(
                        columnName,
                        typeHandler.getResult(rsw.getResultSet(), columnName)
                );
            }else{
                throw new MybatisException("Unrecognized type in handle dynaClass,type:["+ dynaColumn.getType()+"]");
            }
        }
        return dynaBean;
    }

    @Override
    public Cursor<Object> handleCursorResultSets(Statement stmt) throws SQLException {
        throw new UnsupportedOperationException("can't handle cursor in DynaBeanResultSetHandler");
    }

    @Override
    public void handleOutputParameters(CallableStatement cs) throws SQLException {
        throw new UnsupportedOperationException("can't handle output parameter in DynaBeanResultSetHandler");
    }
}
