package com.goodworkalan.go.go;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Redirection {
    public final ByteArrayOutputStream err;
    
    public final ByteArrayOutputStream out;
    
    public final InputOutput io;
    
    public Redirection() {
        this.err = new ByteArrayOutputStream();
        this.out = new ByteArrayOutputStream();
        this.io = new InputOutput(new ByteArrayInputStream(new byte[0]), new PrintStream(out), new PrintStream(err));
    }
}
