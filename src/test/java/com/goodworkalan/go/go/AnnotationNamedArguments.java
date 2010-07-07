package com.goodworkalan.go.go;

/**
 * A command for testing arguments with annotation specified names.
 *
 * @author Alan Gutierrez
 */
public class AnnotationNamedArguments implements Commandable {
    /** The annotation named field. */
    @Argument("bar")
    public int foo;
    
    /** The annotation named setter. */
    @Argument("foo")
    public void addBar(String bar) {
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
