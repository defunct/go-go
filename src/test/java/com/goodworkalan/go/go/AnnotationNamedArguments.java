package com.goodworkalan.go.go;

// TODO Document.
public class AnnotationNamedArguments implements Commandable {
    // TODO Document.
    @Argument("bar")
    public int foo;
    
    // TODO Document.
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
