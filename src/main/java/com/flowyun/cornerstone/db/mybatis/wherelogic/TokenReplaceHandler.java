package com.flowyun.cornerstone.db.mybatis.wherelogic;

import com.flowyun.cornerstone.db.mybatis.util.MybatisStringUtils;
import org.apache.ibatis.parsing.TokenHandler;

/*
* 将#{}中的部分添加合适的前缀
* */
public class TokenReplaceHandler implements TokenHandler {
    private final String namespace;

    public TokenReplaceHandler(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String handleToken(String content) {
        return "#{"+namespace +"."+ MybatisStringUtils.trim(content)+"}";
    }
}
