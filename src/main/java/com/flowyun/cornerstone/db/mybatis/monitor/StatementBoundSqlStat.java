package com.flowyun.cornerstone.db.mybatis.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatementBoundSqlStat {

    private final String boundSql;
    private final AtomicLong executeCount = new AtomicLong(0);
    private final AtomicLong executeSlowCount = new AtomicLong(0);
    private final AtomicInteger runningCount = new AtomicInteger(0);
    private final AtomicInteger concurrentMax = new AtomicInteger(0);

    public StatementBoundSqlStat(String boundSql) {
        this.boundSql = boundSql;
    }

    void incrementExecuteCount(){
        executeCount.incrementAndGet();
    }
    void incrementRunningCount(){
        int running = runningCount.incrementAndGet();

        /*
        * 刷新最高并发数
        * */
        for (;;) {
            int max = concurrentMax.get();
            if (running > max) {
                if (concurrentMax.compareAndSet(max, running)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    void decrementRunningCount(){
        runningCount.decrementAndGet();
    }

    void incrementExecuteSlowCount(){
        executeSlowCount.incrementAndGet();
    }

    public String getBoundSql() {
        return boundSql;
    }

    public long getExecuteCount() {
        return executeCount.get();
    }

    public long getExecuteSlowCount() {
        return executeSlowCount.get();
    }

    public int getRunningCount() {
        return runningCount.get();
    }

    public int getConcurrentMax() {
        return concurrentMax.get();
    }

}
