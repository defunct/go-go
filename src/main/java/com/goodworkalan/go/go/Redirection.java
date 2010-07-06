package com.goodworkalan.go.go;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// TODO Document.
public class Redirection {
    // TODO Document.
    public final ByteArrayOutputStream err;
    
    // TODO Document.
    public final ByteArrayOutputStream out;
    
    // TODO Document.
    public final InputOutput io;
    
    // TODO Document.
    public Redirection() {
        this.err = new ByteArrayOutputStream();
        this.out = new ByteArrayOutputStream();
        this.io = new InputOutput(new ByteArrayInputStream(new byte[0]), new PrintStream(out), new PrintStream(err));
    }
}
