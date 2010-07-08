package com.goodworkalan.go.go;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

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
     * Create an I/O bouquet that has offers the given input in the input stream
     * and discards all error and standard output.
     * 
     * @param input
     *            The input for the input stream.
     * @param encoding
     *            The encoding of the input stream.
     * @return An I/O bouquet with the given input in the input stream and null
     *         error and output streams.
     */
    static final InputOutput nulls(String input, String encoding) {
        PrintStream out = new PrintStream(new NullOutputStream());
        try {
            return new InputOutput(new ByteArrayInputStream(input.getBytes(encoding)), out, out);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an I/O bouquet that has offers the given input in the input stream
     * and discards all error and standard output.
     * 
     * @param input
     *            The input for the input stream.
     * @return An I/O bouquet with the given input in the input stream and null
     *         error and output streams.
     */
    public static final InputOutput nulls(String input) {
        return nulls(input, "UTF-8");
    }

    /**
     * Create an I/O bouquet with no input the input stream and discards all
     * error and standard output.
     * 
     * @return An I/O bouquet with no input in the input stream and null error
     *         and output streams.
     */
    public static final InputOutput nulls() {
        return nulls("");
    }

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
