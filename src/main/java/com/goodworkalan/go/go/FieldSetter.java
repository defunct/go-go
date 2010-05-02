package com.goodworkalan.go.go;

import java.lang.reflect.Member;

import com.goodworkalan.reflective.Field;
import com.goodworkalan.reflective.ReflectiveException;

public class FieldSetter implements Setter {
    private final Field field;
    
    public FieldSetter(Field field) {
        this.field = field;
    }
    
    /**
     * Set the field associated with this setter in the given object to the given value.
     */
    public void set(Object object, Object value) throws ReflectiveException {
        field.set(object, value);
    }
    
    /**
     * Return the underlying Java reflection field.
     * 
     * @return The underlying field.
     */
    public Member getNative() {
        return field.getNative();
    }

    /**
     * Get the property type.
     * 
     * @return The property type.
     */
    public Class<?> getType() {
        return field.getNative().getType();
    }
}
