package com.econage.core.db.mybatis.mapper.sqlsource;

import org.apache.commons.lang3.StringUtils;
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
        return "#{"+namespace +"."+ StringUtils.trim(content)+"}";
    }
}
