package com.goodworkalan.go.go;

import java.util.List;

/**
 * Static utility methods that perform a few, seemingly necessary, always
 * regrettable, unchecked casts.
 * 
 * @author Alan Gutierrez
 */
class Casts {
    @SuppressWarnings("unchecked")
    public static Class<? extends Task> taskClass(Class taskClass) {
        return taskClass;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Arguable> arguableClass(Class arguableClass) {
        return arguableClass;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Output> outputClass(Class outputClass) {
        return outputClass;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> objectList(List list) {
        return list;
    }

    /**
     * Convert the type into an object type if the type is a primitive type.
     * 
     * @param primitive
     *            A possible primitive type.
     * @return An object type.
     */
    public static Class<?> objectify(Class<?> primitive) {
        if (boolean.class.equals(primitive)) {
            return Boolean.class;
        } else if (byte.class.equals(primitive)) {
            return Byte.class;
        } else if (short.class.equals(primitive)) {
            return Short.class;
        } else if (int.class.equals(primitive)) {
            return Integer.class;
        } else if (long.class.equals(primitive)) {
            return Long.class;
        } else if (double.class.equals(primitive)) {
            return Double.class;
        } else if (float.class.equals(primitive)) {
            return Float.class;
        }
        return primitive;
    }
}
