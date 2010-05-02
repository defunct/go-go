package com.goodworkalan.go.go;

/**
 * When an exception is thrown that implements this interface, the program will
 * exit with the specified exit code and the exception message will be printed
 * to standard error without a stack trace.
 * <p>
 * Restate usage?
 * 
 * @author Alan Gutierrez
 */
public interface Erroneous {
    /**
     * The exit code.
     * 
     * @return The exit code.
     */
    public int getExitCode();
}
