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
     * @param environment The execution environment.
     */
    @Override
    public void execute(Environment environment) {
        environment.out.println("Hello, World!");
    }
}
