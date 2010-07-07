package com.goodworkalan.go.go;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Creates an <code>InputOutput</code> that uses
 * <code>ByteArrayOutputStream</code> instances for standard error and standards
 * out. The <code>InputOutput</code> and output streams are available as fields.
 * 
 * @author Alan Gutierrez
 */
public class Redirection {
    /** The error stream output. */
    public final ByteArrayOutputStream err;
    
    /** The standard output stream output. */
    public final ByteArrayOutputStream out;
    
    /** The I/O bouquet. */
    public final InputOutput io;
    
    /** Create a redirection. */
    public Redirection() {
        this.err = new ByteArrayOutputStream();
        this.out = new ByteArrayOutputStream();
        this.io = new InputOutput(new ByteArrayInputStream(new byte[0]), new PrintStream(out), new PrintStream(err));
    }
}
