package com.goodworkalan.go.go;

import java.io.PrintStream;

/**
 * A strategy for handling runtime errors based on faulty user inputs.
 * 
 * @author Alan Gutierrez
 */
public class ErrorCatcher {
    /**
     * Inspect the given error by either handling it and returning and error
     * code or by re-throwing it.
     * 
     * @param e
     *            The error.
     * @param err
     *            The error message stream.
     * @param out
     *            The output message stream.
     * @return The error code to return when exiting.
     */
    public int inspect(GoError e, PrintStream err, PrintStream out) {
        err.println(e.getMessage());
        return 1;
    }
}
