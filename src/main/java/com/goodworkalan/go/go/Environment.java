package com.goodworkalan.go.go;

/**
 * The environment in which a task is executed.
 * 
 * @author Alan Gutierrez
 */
public class Environment {
    /** The input/output streams. */
    public final InputOutput io;

    /** The current execution. */
    public final Executor executor;

    /** The command part for the current task. */
    public final CommandPart part;
    
    /**
     * Create a new environment.
     * 
     * @param io
     *            The input/output streams.
     * @param part
     *            The command part for the current task.
     * @param execution
     *            The execution state.
     */
    public Environment(InputOutput io, CommandPart part, Executor executor) {
        this.io = io;
        this.part = part;
        this.executor = executor;
    }
}
