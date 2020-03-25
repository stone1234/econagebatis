package com.flowyun.cornerstone.db.mybatis.mapper.dyna.entity;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynaBean {

    private final DynaClass dynaClass;
    protected HashMap<String, Object> values = new HashMap<String, Object>();

    public DynaBean(DynaClass dynaClass) {
        if(dynaClass==null){
            throw new IllegalArgumentException("dynaClass is null");
        }
        this.dynaClass = dynaClass;
    }

    public boolean contains(String name){
        return values.containsKey(name);
    }

    public DynaClass getDynaClass(){
        return dynaClass;
    }

    public Object getPkValue(){
        return get(dynaClass.getIdColumn());
    }
    public Object getFkValue(){
        return get(dynaClass.getFkColumn());
    }

    public Object get(String name){
        // Return any non-null value for the specified property
        final Object value = values.get(name);
        if (value != null) {
            return (value);
        }

        // Return a null value for a non-primitive property
        final Class<?> type = getDynaProperty(name).getType();
        if (!type.isPrimitive()) {
            return (value);
        }

        // Manufacture default values for primitive properties
        if (type == Boolean.TYPE) {
            return (Boolean.FALSE);
        } else if (type == Byte.TYPE) {
            return ((byte) 0);
        } else if (type == Character.TYPE) {
            return ((char) 0);
        } else if (type == Double.TYPE) {
            return (0.0);
        } else if (type == Float.TYPE) {
            return ((float) 0.0);
        } else if (type == Integer.TYPE) {
            return (0);
        } else if (type == Long.TYPE) {
            return (0L);
        } else if (type == Short.TYPE) {
            return ((short) 0);
        } else {
            return (null);
        }
    }

    public void set(String name, Object value){

        final DynaColumn descriptor = getDynaProperty(name);
        if (value == null) {
            if (descriptor.getType().isPrimitive()) {
                throw new NullPointerException
                        ("Primitive value for '" + name + "'");
            }
        } else if (!isAssignable(descriptor.getType(), value.getClass())) {
            throw new IllegalArgumentException
                    ("Cannot assign value of type '" +
                            value.getClass().getName() +
                            "' to property '" + name + "' of type '" +
                            descriptor.getType().getName() + "'");
        }
        values.put(name, value);

    }

    public Map<String, Object> getValues(){
        return Collections.unmodifiableMap(values);
    }

    protected DynaColumn getDynaProperty(final String name) {

        final DynaColumn descriptor = getDynaClass().getDynaProperty(name);
        if (descriptor == null) {
            throw new IllegalArgumentException
                    ("Invalid property name '" + name + "'");
        }
        return (descriptor);

    }

    protected boolean isAssignable(final Class<?> dest, final Class<?> source) {

        if (dest.isAssignableFrom(source) ||
                ((dest == Boolean.TYPE) && (source == Boolean.class)) ||
                ((dest == Byte.TYPE) && (source == Byte.class)) ||
                ((dest == Character.TYPE) && (source == Character.class)) ||
                ((dest == Double.TYPE) && (source == Double.class)) ||
                ((dest == Float.TYPE) && (source == Float.class)) ||
                ((dest == Integer.TYPE) && (source == Integer.class)) ||
                ((dest == Long.TYPE) && (source == Long.class)) ||
                ((dest == Short.TYPE) && (source == Short.class))) {
            return (true);
        } else {
            return (false);
        }

    }
}
