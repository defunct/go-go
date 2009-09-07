package com.goodworkalan.go.go;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * An environment structure builder for easily specifying an environment. 
 * 
 * @author Alan Gutierrez
 */
public class EnvironmentBuilder {
    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private String[][] arguments = new String[0][];
    private String[] remaining = new String[0];
    
    public EnvironmentBuilder() {
    }
    
    public void setIn(InputStream in) {
        this.in = in;
    }
    
    public void setOut(PrintStream out) {
        this.out = out;
    }
    
    public void setErr(PrintStream err) {
        this.err = err;
    }
    
    public void setArguments(String[][] arguments) {
        this.arguments = arguments;
    }
    
    public void setRemaining(String[] remaining) {
        this.remaining = remaining;
    }
    
    public Environment getInstance() {
        return new Environment(in, err, out, arguments, remaining);
    }
}
