package com.econage.core.db.mybatis.annotations;

import com.econage.core.db.mybatis.wherelogic.WhereLogicParser;

import java.lang.annotation.*;

/**
 * <p>
 * 通用查询条件表单字段信息
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface WhereLogicField {

    /*
     * 解析器类，优先级最高
     * 如果有解析器类，则忽略其他解析参数
     * */
    Class<? extends WhereLogicParser> parser() default WhereLogicParser.class;

    /*
    * 是否可以用在谓语语句中
    * */
    boolean enable() default true;

    /*
    * 映射的数据库列名称
    * 当wherePart有值，column无效
    * */
    String column() default "";

    /*
    * 如果自动设置的谓语语句不满足，可以手动设计谓语语句
    * 当wherePart不为空时，column无效
    * */
    String wherePart() default "";

    /*
     * 如果当前列是原型数据，是否识使用默认的值
     * 例如控制int类型数据，是否使用0
     * */
    boolean usePrimitiveZero() default false;

}
