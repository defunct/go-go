package com.goodworkalan.go.go;

/**
 * A task that is always present to test a Jav-a-Go-Go installation.
 *
 * @author Alan Gutierrez
 */
@Command(parent = BootCommand.class)
public class HelloCommand implements Commandable {
    /**
     * Print "Hello, World!"
     * 
     * @param env The execution environment.
     */
    public void execute(Environment env) {
        env.io.out.println("Hello, World!");
    }
}
