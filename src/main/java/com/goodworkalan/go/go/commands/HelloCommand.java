package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * A task that is always present to test a Jav-a-Go-Go installation use to check
 * that Jav-a-Go-Go exists.
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
