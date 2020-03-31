package com.flowyun.cornerstone.db.mybatis.monitor;

import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;
import com.flowyun.cornerstone.db.mybatis.util.MybatisSystemClock;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;

public class StatementInvocationHandler implements InvocationHandler {

    private static final String[] EXECUTE_METHOD_ARRAY = {
            "execute","executeBatch",
            "executeQuery","executeUpdate",
            "executeLargeBatch","executeLargeUpdate",
    };

    private final Statement stmt;
    private final StatementMonitor monitor;
    private final MappedStatement ms;
    private final BoundSql boundSql;

    public StatementInvocationHandler(
            Statement stmt, StatementMonitor monitor,
            MappedStatement ms, BoundSql boundSql
    ) {
        this.stmt = stmt;
        this.monitor = monitor;
        this.ms = ms;
        this.boundSql = boundSql;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        boolean needCountExecute = false;
        for(String executeMethod : EXECUTE_METHOD_ARRAY){
            if(MybatisStringUtils.equals(methodName,executeMethod)){
                needCountExecute = true;
                break;
            }
        }
        if(!needCountExecute){
            return method.invoke(stmt,args);
        }else{

            monitor.beforeExecute(ms,boundSql);
            long startMills = MybatisSystemClock.now();
            try{
                return method.invoke(stmt,args);
            }finally{
                long endMills = MybatisSystemClock.now();
                monitor.afterExecute(ms,boundSql,endMills-startMills);
            }

        }
    }
}
