package com.goodworkalan.go.go;

public class AnnotationNamedArguments implements Commandable {
    @Argument("bar")
    public int foo;
    
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
