package com.goodworkalan.go.go;

/**
 * Base command for testing.
 *
 * @author Alan Gutierrez
 */
public class Snap implements Commandable {
    @Argument
    public boolean mississippi;
    
    public void execute(Environment env) {
        ButtonCommand button = env.executor.run(ButtonCommand.class, env.io, "button", env.arguments.get(0));
        if (mississippi && button.saratoga != null) {
            env.io.out.println(button.saratoga);
        }
    }
}
