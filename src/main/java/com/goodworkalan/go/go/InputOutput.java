package com.goodworkalan.go.go;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A structure to gather the input/output streams.
 * 
 * @author Alan Gutierrez
 */
public class InputOutput {
    /** The input stream. */
    public final InputStream in;

    /** The output stream. */
    public final PrintStream out;

    /** The error stream. */
    public final PrintStream err;

    /**
     * Construct an input/output structure using the system input, error, and
     * error streams and the given working directory.
     */
    public InputOutput() {
        this(System.in, System.out, System.err);
    }

    /**
     * Construct an input/output structure.
     * 
     * @param in
     *            The input stream.
     * @param out
     *            The output stream.
     * @param err
     *            The error stream.
     */
    public InputOutput(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.err = err;
        this.out = out;
    }
}
