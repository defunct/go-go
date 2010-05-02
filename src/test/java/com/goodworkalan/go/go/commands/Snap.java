package com.goodworkalan.go.go.commands;

import java.io.File;

import com.goodworkalan.comfort.io.Files;
import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.DirectoryPart;

/**
 * Base command for testing.
 *
 * @author Alan Gutierrez
 */
public class Snap implements Commandable {
    /** An example boolean flag. */
    @Argument
    public boolean mississippi;
    
    /** If true launch hidden processes. */
    @Argument
    public boolean hidden;

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
        if (hidden) {
            env.extendClassPath(new DirectoryPart(Files.file(new File("."), "src", "main", "java").getAbsoluteFile()));
            env.invokeAfter(new Commandable() {
                public void execute(Environment env) {
                    env.extendClassPath(new DirectoryPart(Files.file(new File("."), "src", "main", "java").getAbsoluteFile()));
                    env.invokeAfter(new Commandable() {
                        public void execute(Environment env) {
                            env.extendClassPath(new DirectoryPart(Files.file(new File("."), "src", "main", "resources").getAbsoluteFile()));
                        }
                    });
                }
            });
        }
    }
}
