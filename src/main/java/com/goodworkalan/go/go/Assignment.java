package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.ASSIGNMENT_EXCEPTION_THROWN;
import static com.goodworkalan.go.go.GoException.ASSIGNMENT_FAILED;

import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;

/**
 * Assigns a command line argument to a property in a {@link Task} Bean.
 * 
 * @author Alan Gutierrez
 */
class Assignment {
    /** The class that declared the assignment. */
    private final Class<? extends Arguable> declaringClass;
    
    /** The method to set the task property. */
    private final Method setter;

    /** The converter for the string property. */
    private final Converter converter;

    /**
     * Create an assignment that will set the property identified by the given
     * setter method with a string value converted with the given converter.
     * 
     * @param declaringClass
     *            The class that declared the assignment.
     * @param setter
     *            The property setter.
     * @param converter
     *            The string converter.
     */
    public Assignment(Class<? extends Arguable> declaringClass, Method setter, Converter converter) {
        this.declaringClass = declaringClass;
        this.setter = setter;
        this.converter = converter;
    }

    public Object convertValue(String value) {
        return converter.convert(value);
    }

    /**
     * Set the property of the task that is defined by this assignment to the
     * value represented by the given string.
     * 
     * @param arguable
     *            The task.
     * @param value
     *            The string value.
     */
    public void setValue(Arguable arguable, Object value) {
        try {
            try {
                setter.invoke(arguable, value);
            } catch (ReflectiveException e) {
                if (e.getCode() == ReflectiveException.INVOCATION_TARGET) {
                    throw new GoException(ASSIGNMENT_EXCEPTION_THROWN, e);
                }
                throw new GoException(ASSIGNMENT_FAILED, e);
            }
        } catch (GoException e) {
            throw e.put("targetType", getType()).put("setter", setter.getNative().getName());
        }
    }

    /**
     * Get the type of the declaring class.
     * 
     * @return The type of the declaring class.
     */
    public Class<? extends Arguable> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Get the assignment type.
     * 
     * @return The assignment type.
     */
    public Class<?> getType() {
        return setter.getNative().getParameterTypes()[0];
    }
}
