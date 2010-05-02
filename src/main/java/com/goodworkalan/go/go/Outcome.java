package com.goodworkalan.go.go;

/**
 * Structure to contain execution output.
 *
 * @author Alan Gutierrez
 */
class Outcome {
    /** The exit code. */
    public final int code;
    
    /** The return object. */
    public final Object object;

    /**
     * Create a new outcome with the given code and the given object.
     * 
     * @param code
     *            The exit code.
     * @param object
     *            The return object.
     */
    public Outcome(int code, Object object) {
        this.code = code;
        this.object = object;
    }
}
