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

    public GoError(int code) {
        super(code);
    }
    
    public GoError(int code, Throwable cause) {
        super(code, cause);
    }
}
