package com.goodworkalan.go.go;

/**
 * A structure that associates a qualified argument name with a value created
 * from the string value.
 * 
 * @author Alan Gutierrez
 */
class Conversion {
    /** The command name. */
    public final String command;

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
    public Conversion(String command, String name, Object value) {
        this.command = command;
        this.name = name;
        this.value = value;
    }
}
