package com.goodworkalan.go.go;

/**
 * A task that is always present to test a Jav-a-Go-Go installation.
 *
 * @author Alan Gutierrez
 */
public class Hello extends Task {
    /**
     * Print "Hello, World!"
     * 
     * @param env The execution environment.
     */
    @Override
    public void execute(Environment env) {
        env.io.out.println("Hello, World!");
    }
}
