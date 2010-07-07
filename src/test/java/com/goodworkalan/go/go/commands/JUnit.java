package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * Example of a command.
 *
 * @author Alan Gutierrez
 */
@Command(name = "junit", parent = Snap.class)
public class JUnit implements Commandable {
    /**
     * Does nothing.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
    }
}
