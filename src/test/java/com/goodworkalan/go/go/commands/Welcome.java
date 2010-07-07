package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * A command that prints a greeting.
 *
 * @author Alan Gutierrez
 */
@Command(parent = Snap.class)
public class Welcome implements Commandable {
    /** The greeting. */
    public String greeting;
    
    /**
     * Set the greeting.
     * 
     * @param greeting
     *            The greeting.
     */
    @Argument
    public void addGreeting(String greeting) {
        this.greeting = greeting;
    }

    /**
     * Print the greeting to standard out.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
        env.io.out.println(greeting);
    }
}