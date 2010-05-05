package com.goodworkalan.go.go;

/**
 * The interface common to all Jav-a-Go-Go commands.
 *
 * @author Alan Gutierrez
 */
public interface Commandable {
    /**
     * Perform an action within the given enviornment.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env);
}
