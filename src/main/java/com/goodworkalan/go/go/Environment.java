package com.goodworkalan.go.go;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * The environment in which a task is executed.
 * 
 * @author Alan Gutierrez
 */
public class Environment {
    /** The input stream. */
    public final InputStream in;

    /** The error stream. */
    public final PrintStream err;

    /** The output stream. */
    public final PrintStream out;
    
    /** The input/output streams. */
    public final InputOutput io;

    /** The current execution. */
    public final Executor executor;

    /** The command part for the current task. */
    public final CommandPart part;
    
    /**
     * Create a new environment.
     * 
     * @param in
     *            The input stream.
     * @param err
     *            The error stream.
     * @param out
     *            The output stream.
     * @param commandPart
     *            The command part for the current task.
     * @param execution
     *            The execution state.
     */
    public Environment(InputStream in, PrintStream err, PrintStream out, CommandPart commandPart, Executor executor) {
        this.io = new InputOutput(in, out, err);
        this.in = in;
        this.err = err;
        this.out = out;
        this.part = commandPart;
        this.executor = executor;
    }
}
