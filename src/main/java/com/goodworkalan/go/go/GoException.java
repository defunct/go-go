package com.goodworkalan.go.go;

public class GoException extends RuntimeException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

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
}
