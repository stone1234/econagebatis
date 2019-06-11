package com.econage.core.db.mybatis.wherelogic;


/*
* 查询表单，字段信息，对于
* */
public class WhereLogicFieldInfo {

    private Class<?> type;

    private String property;
    private String column;
    /*
    * 解析后的where语句
    * */
    private String whereLogic;
    /*
    * 如果当前列是原型数据，是否识使用默认的值
    * 例如控制int类型数据，是否使用0
    * */
    private boolean usePrimitiveZero;
    /*
     * 是否集合类
     * */
    private boolean isCollectionType;
    /*
     * 是否数组
     * */
    private boolean isArrayType;
    /*
    * 是否原型
    * */
    private boolean primitiveType;
    /*
    * 把解析逻辑委托给某个解析器对象
    * */
    private WhereLogicParser whereLogicParser;


    public boolean isCollectionType() {
        return isCollectionType;
    }

    public void setCollectionType(boolean collectionType) {
        isCollectionType = collectionType;
    }

    public boolean isArrayType() {
        return isArrayType;
    }

    public void setArrayType(boolean arrayType) {
        isArrayType = arrayType;
    }

    public boolean isPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(boolean primitiveType) {
        this.primitiveType = primitiveType;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getWhereLogic() {
        return whereLogic;
    }

    public void setWhereLogic(String whereLogic) {
        this.whereLogic = whereLogic;
    }

    public boolean isUsePrimitiveZero() {
        return usePrimitiveZero;
    }

    public void setUsePrimitiveZero(boolean usePrimitiveDefaultVal) {
        this.usePrimitiveZero = usePrimitiveDefaultVal;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public WhereLogicParser getWhereLogicParser() {
        return whereLogicParser;
    }

    public void setWhereLogicParser(WhereLogicParser whereLogicParser) {
        this.whereLogicParser = whereLogicParser;
    }
}
