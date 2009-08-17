package com.goodworkalan.go.go;

/**
 * A single name value pair given to a command.
 * 
 * @author Alan Gutierrez
 */
public class Parameter {
    /** The command name. */
    private final String command;

    /** The parameter name. */
    private final String name;
    
    /** The parameter value. */
    private final String value;

    /**
     * Create a name value pair.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     */
    public Parameter(String name, String value) {
        this.command = getCommand(name);
        this.name = getName(name);
        this.value = value;
    }
    
    private static String getCommand(String name) {
        int colon = name.indexOf(':');
        if (colon == -1) {
            return name;
        }
        return name.substring(0, colon);
    }
    
    private static String getName(String name) {
        int colon = name.indexOf(':');
        if (colon == -1) {
            return name;
        }
        return name.substring(colon + 1);
    }
    
    public boolean matches(String command, String name) {
        return name.equals(this.name) && (this.command.length() == 0 || command.equals(this.command)); 
    }
    
    /**
     * Get the parameter name.
     * 
     * @return The parameter name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parameter value.
     * 
     * @return The parameter value.
     */
    public String getValue() {
        return value;
    }

    /**
     * A parameter is equal to the given object if the object is also a
     * parameter and the name and values are equal.
     * 
     * @return True if the parameter is equal to the given object.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Parameter) {
            Parameter parameter = (Parameter) object;
            return name.equals(parameter.name) && value.equals(parameter.value);
        }
        return false;
    }
    
    /**
     * Create a hash code from the name and value of the parameter.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int hashCode = 3719;
        hashCode = hashCode * 37 + name.hashCode();
        hashCode = hashCode * 37 + value.hashCode();
        return hashCode;
    }
    
    /**
     * Create a string representation of this parameter suitable for debugging.
     * 
     * @return A string representation of the parameter.
     */
    @Override
    public String toString() {
        return "--" + name + "=" + value;
    }
}
