package com.goodworkalan.go.go;

/**
 * An example of a command with a lot of fields and methods that look like, but
 * are not arguments.
 * 
 * @author Alan Gutierrez
 */
public class NoArguments implements Commandable {
    /** Starts with add but does not name an argument. */
    public void add(int integer) {
    }
    
    /** Starts with add but has two parameters. */
    public void addSomething(int one, int two) {
    }

    /** Looks like an argument but is not visible. */
    @Argument
    protected void addProtected(int one) {
    }

    /** No argument annotation. */
    public void addNotArguable(int one) {
    }

    /**
     * Do nothing.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
    }
}
