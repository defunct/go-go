package com.goodworkalan.go.go;

import java.lang.reflect.Member;

import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;

/**
 * Sets an object property using a single object void method.
 *
 * @author Alan Gutierrez
 */
class MethodSetter implements Setter {
    /** The single argument void method. */
    private final Method method;

    /**
     * Create a method setter that uses the given single argument void method to
     * set an object property.
     * 
     * @method The setter method.
     */
    public MethodSetter(Method method) {
        this.method = method;
    }

    /**
     * Set the property associated with this setter in the given object to the
     * given value.
     * 
     * @param object
     *            The object.
     * @param value
     *            The property value.
     */
    public void set(Object object, Object value) throws ReflectiveException {
        method.invoke(object, value);
    }
    
    /**
     * Return the underlying Java reflection method.
     * 
     * @return The underlying method.
     */
    public Member getNative() {
        return method.getNative();
    }
    
    /**
     * Get the property type.
     * 
     * @return The property type.
     */
    public Class<?> getType() {
        return method.getNative().getParameterTypes()[0];
    }
}
