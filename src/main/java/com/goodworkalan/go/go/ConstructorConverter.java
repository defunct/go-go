package com.goodworkalan.go.go;
import static com.goodworkalan.go.go.GoException.CANNOT_CREATE_FROM_STRING;
import static com.goodworkalan.go.go.GoException.*;

import com.goodworkalan.reflective.Constructor;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

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
    public ConstructorConverter(ReflectiveFactory reflectiveFactory, Class<?> targetClass) {
        try {
            this.constructor = reflectiveFactory.getConstructor(targetClass, String.class);
        } catch (ReflectiveException e) {
            throw new GoException(CANNOT_CREATE_FROM_STRING, e, targetClass);
        }
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
            return constructor.newInstance(string);
        } catch (ReflectiveException e) {
            if (e.getCode() == ReflectiveException.INVOCATION_TARGET) {
                if (e.getCause().getCause() instanceof IllegalArgumentException) {
                    throw new GoException(STRING_CONVERSION_ERROR, e, constructor.getNative().getDeclaringClass(), string);
                }
            }
            throw new GoException(STRING_CONSTRUCTOR_ERROR, e, constructor.getNative().getDeclaringClass(), string);
        }
    }
}
