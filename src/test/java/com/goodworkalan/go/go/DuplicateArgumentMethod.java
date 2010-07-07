package com.goodworkalan.go.go;

/**
 * An invalid command that duplicates an argument.
 *
 * @author Alan Gutierrez
 */
public class DuplicateArgumentMethod extends BaseCommand implements Commandable {
    /**
     * Add a value.
     * 
     * @param value
     *            The value.
     */
    @Argument
    public void addValue(int value) {
    }

    /**
     * Does nothing.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
    }
}
