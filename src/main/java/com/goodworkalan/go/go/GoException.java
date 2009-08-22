package com.goodworkalan.go.go;

public class GoException extends RuntimeException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with the given error code.
     * 
     * @param code
     *            The error code.
     */
    public GoException(int code) {
        super();
    }

    /**
     * Create a mix exception with the given error code and cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The wrapped exception.
     */
    public GoException(int code, Throwable cause) {
        super(cause);
    }
    
    public GoException add(String...argument) {
        return this;
    }
    
    /** A Task has multiple Task type attributes indicating multiple parents. */
    public final static int MULTIPLE_TASK_PARENTS = 102;
    
    /** A property value ends with a backslash character, does not actually escape anything. */
    public final static int TERMINAL_BACKSLASH = 202;
    
    
    /** A variable substitution in the property file creates an infinite loop. */
    public final static int PROPERTY_LOOP = 101;
}
