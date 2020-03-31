package com.flowyun.cornerstone.db.mybatis.monitor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementRoutingHandler extends RoutingStatementHandler {

    private final StatementMonitor monitor;
    private final MappedStatement ms;
    private final BoundSql boundSql;

    public StatementRoutingHandler(
            StatementMonitor monitor,
            Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql
    ) {
        super(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        this.monitor = monitor;
        this.ms = ms;
        this.boundSql = boundSql;
    }

    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        return monitor.proxyStatement(
                super.prepare(connection, transactionTimeout)
                ,ms,boundSql
        );
    }

}
