package com.goodworkalan.go.go;

/**
 * FIXME We want to be able to have an exception, so that a command line
 * switch will turn errors into stack traces.
 * 
 * @author Alan Gutierrez
 */
public class GoError extends GoException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;
    
    /** No arguments in the command line. */
    public static int COMMAND_LINE_NO_ARGUMENTS = 1001;

    /**
     * Create a go error with the given error code.
     * 
     * @param code The error code.
     */
    public GoError(int code) {
        super(code);
    }

    /**
     * Create a go error with the given error code and the given cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The cause.
     */
    public GoError(int code, Throwable cause) {
        super(code, cause);
    }
}
