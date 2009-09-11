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

    /** The arguments for each command with command name. */
    public final String[][] arguments;

    /** The remaining arguments. */
    public final String[] remaining;
    
    /** The command interpreter. */
    public final CommandInterpreter commandInterpreter;

    /**
     * Create a new environment.
     * 
     * @param in
     *            The input stream.
     * @param err
     *            The error stream.
     * @param out
     *            The output stream.
     * @param arguments
     *            The arguments for each command with command name.
     * @param remaining
     *            The remaining arguments.
     */
    public Environment(CommandInterpreter commandInterpreter, InputStream in, PrintStream err, PrintStream out, String[][] arguments, String[] remaining) {
        this.commandInterpreter = commandInterpreter;
        this.in = in;
        this.err = err;
        this.out = out;
        this.arguments = arguments;
        this.remaining = remaining;
    }
}
