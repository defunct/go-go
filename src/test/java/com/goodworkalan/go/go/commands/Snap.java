package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * Base command for testing.
 *
 * @author Alan Gutierrez
 */
public class Snap implements Commandable {
    /** An example boolean flag. */
    @Argument
    public boolean mississippi;

    /**
     * Test executing a sibling command.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
        ButtonCommand button = env.executor.run(ButtonCommand.class, env.io, "button", env.arguments.get(0));
        if (mississippi && button.saratoga != null) {
            env.io.out.println(button.saratoga);
        }
        env.output("Snap was here!");
    }
}
