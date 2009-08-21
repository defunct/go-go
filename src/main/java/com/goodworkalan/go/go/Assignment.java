package com.goodworkalan.go.go;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Assignment {
    /** The method to set the task property. */
    private final Method setter;

    /** The converter for the string property. */
    private final Converter converter;

    /**
     * Create an assignment that will set the property indentified by the given
     * setter method with a string value converted with the given converter.
     * 
     * @param setter
     *            The property setter.
     * @param converter
     *            The string converter.
     */
    public Assignment(Method setter, Converter converter) {
        this.setter = setter;
        this.converter = converter;
    }

    /**
     * Set the property of the task that is defined by this assignment to the
     * value represented by the given string.
     * 
     * @param task
     *            The task.
     * @param value
     *            The string value.
     */
    public void setValue(Task task, String value) {
        try {
            setter.invoke(task, new Object[] { converter.convert(value) });
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new GoException(0, e);
        } catch (Exception e) {
            throw new GoException(0, e);
        }
    }

    /**
     * Get the assignment type.
     * 
     * @return The assignment type.
     */
    public Class<?> getType() {
        return setter.getParameterTypes()[0];
    }
}
