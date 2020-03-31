package com.flowyun.cornerstone.db.mybatis.monitor;

import com.flowyun.cornerstone.db.mybatis.MybatisException;
import com.flowyun.cornerstone.db.mybatis.util.MybatisAssert;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.*;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StatementMonitor {
    private final long slowSqlDuration;
    private final ConcurrentHashMap<String,StatementStat> statementHolder = new ConcurrentHashMap<>();


    private final Constructor<?> statementProxyCons;

    public StatementMonitor(Duration slowSqlDuration) {
        this(slowSqlDuration.toMillis());
    }
    public StatementMonitor(long slowSqlDuration) {
        this.slowSqlDuration = slowSqlDuration;

        Class<?> statementProxyCls = Proxy.getProxyClass(
                StatementInvocationHandler.class.getClassLoader(),
                Statement.class, PreparedStatement.class, CallableStatement.class
        );
        try {
            statementProxyCons = statementProxyCls.getConstructor(InvocationHandler.class);
        } catch (NoSuchMethodException e) {
            throw new MybatisException(e);
        }
        if (!Modifier.isPublic(statementProxyCls.getModifiers())) {
            statementProxyCons.setAccessible(true);
        }

    }

    Statement proxyStatement(
            Statement rawStatement,
            MappedStatement ms, BoundSql boundSql
    ){
        MybatisAssert.notNull(rawStatement,"Statement is null");
        try {
            return (Statement)statementProxyCons.newInstance(new StatementInvocationHandler(
                    rawStatement,this,
                    ms,boundSql
            ));
        } catch (Exception e) {
            throw new MybatisException(e);
        }
    }


    private static Function<String, StatementStat> statementHolderCreator =  StatementStat::new;
    void beforeExecute(MappedStatement ms, BoundSql boundSql) {

        StatementStat statementStat = statementHolder.computeIfAbsent(ms.getId(), statementHolderCreator);
        statementStat.incrementRunningCount(boundSql);
        statementStat.incrementExecuteCount(boundSql);

    }

    void afterExecute(MappedStatement ms, BoundSql boundSql, long mills) {

        StatementStat statementStat = statementHolder.computeIfAbsent(ms.getId(), statementHolderCreator);
        statementStat.decrementRunningCount(boundSql);
        if(slowSqlDuration!=0&&mills>slowSqlDuration){
            statementStat.incrementExecuteSlowCount(boundSql);
        }

    }

    public void reset(){
        statementHolder.clear();
    }

    public Collection<StatementStat> getStatementStat(){
        return Collections.unmodifiableCollection(statementHolder.values());
    }

}
