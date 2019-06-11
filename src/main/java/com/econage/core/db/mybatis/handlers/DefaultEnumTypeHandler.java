package com.econage.core.db.mybatis.handlers;

import com.econage.core.db.mybatis.enums.IEnum;
import com.econage.core.db.mybatis.util.MybatisIEnumUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
* 对枚举做特殊处理，如果枚举实现了IEnum借口，则以接口的返回值作为匹配依据。
* 否则以枚举名字作为依据
* */
public class DefaultEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private final Class<E> type;
    private final EnumTypeHandler<E> mybatisDefaultEnumTypeHandler;
    private final boolean isIEnum;

    public DefaultEnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.isIEnum = IEnum.class.isAssignableFrom(type);
        this.mybatisDefaultEnumTypeHandler = new EnumTypeHandler<>(type);
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if(isIEnum){
            IEnum iEnum = (IEnum)parameter;
            if (jdbcType == null) {
                ps.setObject(i, iEnum.getValue());
            } else {
                ps.setObject(i, iEnum.getValue(), jdbcType.TYPE_CODE);
            }
        }else{
            mybatisDefaultEnumTypeHandler.setNonNullParameter(ps,i,parameter,jdbcType);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if(null == rs.getString(columnName) && rs.wasNull()){
            return null;
        }
        if(isIEnum){
            return (E)MybatisIEnumUtils.valueOf(this.type, rs.getObject(columnName));
        }else{
            return mybatisDefaultEnumTypeHandler.getNullableResult(rs,columnName);
        }

    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if(null == rs.getString(columnIndex) && rs.wasNull()){
            return null;
        }
        if(isIEnum){
            return (E)MybatisIEnumUtils.valueOf(this.type, rs.getObject(columnIndex));
        }else{
            return mybatisDefaultEnumTypeHandler.getNullableResult(rs,columnIndex);
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if(null == cs.getString(columnIndex) && cs.wasNull()){
            return null;
        }
        if(isIEnum){
            return (E)MybatisIEnumUtils.valueOf(this.type, cs.getObject(columnIndex));
        }else{
            return mybatisDefaultEnumTypeHandler.getNullableResult(cs,columnIndex);
        }
    }
}
