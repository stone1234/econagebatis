package com.econage.core.db.mybatis.handlers;

import com.econage.core.db.mybatis.util.MybatisStringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;


public class LocaleTypeHandler extends BaseTypeHandler<Locale> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Locale parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i,parameter.toLanguageTag());
    }

    @Override
    public Locale getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String localeStr = rs.getString(columnName);
        return toLocale(localeStr);
    }

    @Override
    public Locale getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String localeStr = rs.getString(columnIndex);
        return toLocale(localeStr);
    }

    @Override
    public Locale getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String localeStr = cs.getString(columnIndex);
        return toLocale(localeStr);
    }

    private Locale toLocale(String localeStr){
        if(MybatisStringUtils.isEmpty(localeStr)){
            //如果本地语言为空，则处理为简体中文;
            return Locale.CHINA;
        }else{
            return Locale.forLanguageTag(localeStr);
        }
    }

}
