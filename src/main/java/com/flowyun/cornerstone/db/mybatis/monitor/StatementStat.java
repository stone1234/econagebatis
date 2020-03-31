package com.flowyun.cornerstone.db.mybatis.monitor;

import org.apache.ibatis.mapping.BoundSql;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StatementStat {

    private final String mappedStatementId;
    private final ConcurrentHashMap<String,StatementBoundSqlStat> bondSqlHolder = new ConcurrentHashMap<>();

    public StatementStat(String mappedStatementId) {
        this.mappedStatementId = mappedStatementId;
    }

    private static Function<String, StatementBoundSqlStat> bondSqlHolderCreator =  StatementBoundSqlStat::new;
    private StatementBoundSqlStat getBoundSqlStat(BoundSql boundSql){
        return bondSqlHolder.computeIfAbsent(boundSql.getSql(),bondSqlHolderCreator);
    }

    public String getMappedStatementId() {
        return mappedStatementId;
    }

    public Collection<StatementBoundSqlStat> getStatementBoundSqlStat() {
        return Collections.unmodifiableCollection(bondSqlHolder.values());
    }

    void incrementExecuteCount(BoundSql boundSql){
        getBoundSqlStat(boundSql).incrementExecuteCount();
    }

    void incrementRunningCount(BoundSql boundSql){
        getBoundSqlStat(boundSql).incrementRunningCount();
    }

    void decrementRunningCount(BoundSql boundSql){
        getBoundSqlStat(boundSql).decrementRunningCount();
    }

    void incrementExecuteSlowCount(BoundSql boundSql){
        getBoundSqlStat(boundSql).incrementExecuteSlowCount();
    }

}
