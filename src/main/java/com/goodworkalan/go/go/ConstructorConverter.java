package com.goodworkalan.go.go;

import java.lang.reflect.Constructor;

/**
 * A converter that converts a string by constructing an object using an
 * reflection constructor with a single string parameter.
 * 
 * @author Alan Gutierrez
 */
public class ConstructorConverter implements Converter {
    /** A constructor that takes a single string as a parameter. */
    private final Constructor<?> constructor;

    /**
     * Create a constructor converter with the given constructor.
     * 
     * @param constructor
     *            The constructor.
     */
    public ConstructorConverter(Constructor<?> constructor) {
        assert constructor.getParameterTypes().length == 1;
        assert constructor.getParameterTypes()[0].equals(String.class);
        this.constructor = constructor;
    }

    /**
     * Convert the given string using by constructing a new object with the
     * constructor of this converter.
     * 
     * @param string
     *            A string value.
     * @return Returns an object conversion.
     * @exception GoException
     *                If the conversion fails.
     */
    public Object convert(String string) {
        try {
            return constructor.newInstance(new Object[] { string });
        } catch (RuntimeException e) {
            throw e;
        } catch (InstantiationException e) { 
            throw new GoException(0, e);
        } catch (Exception e) {
            throw new GoException(0, e);
        }
    }
}
