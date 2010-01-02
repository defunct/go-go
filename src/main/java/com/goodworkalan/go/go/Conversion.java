package com.goodworkalan.go.go;

/**
 * A structure that associates a qualified argument name with a value created
 * from the string value.
 * 
 * @author Alan Gutierrez
 */
class Conversion {
    /** The qualified argument name. */
    public final String name;
    
    /** The value. */
    public final Object value;

    /**
     * Create a structure for the given argument name and the given converted
     * argument value.
     * 
     * @param name
     *            The qualified argument name.
     * @param value
     *            The argument value.
     */
    public Conversion(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the qualified argument name.
     * 
     * @return The qualified argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the converted argument value.
     * 
     * @return The converted argument value.
     */
    public Object getValue() {
        return value;
    }
}
