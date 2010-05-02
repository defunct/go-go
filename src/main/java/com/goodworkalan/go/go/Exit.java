package com.goodworkalan.go.go;

/**
 * Commands throw this exception to terminate execution normally and return the
 * given exit code.
 * 
 * @author Alan Gutierrez
 */
public class Exit extends RuntimeException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The exit code. */
    final int code;

    /**
     * Exit with the given exit code.
     * 
     * @param code
     *            The exit code.
     */
    public Exit(int code) {
        this.code = code;
    }
}
