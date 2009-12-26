package com.goodworkalan.go.go;

import java.io.File;
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
    
    /** The working directory. */
    public final File dir;

    /**
     * Construct an input/output structure using the system input, error, and
     * error streams and the given working directory.
     */
    public InputOutput(File dir) {
        this(System.in, System.out, System.err, dir);
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
    public InputOutput(InputStream in, PrintStream out, PrintStream err, File dir) {
        this.in = in;
        this.err = err;
        this.out = out;
        this.dir = dir;
    }
    
    public File relativize(File file) {
        if (!file.isAbsolute()) {
            return new File(dir, file.getPath());
        }
        return file;
    }
}
