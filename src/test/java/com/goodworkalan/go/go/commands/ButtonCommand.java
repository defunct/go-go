package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * Top level command called by another command.
 *
 * @author Alan Gutierrez
 */
public class ButtonCommand implements Commandable {
    /** The string property. */
    @Argument
    public String saratoga;
    
    /**
     * Make myself my output.
     */
    public void execute(Environment env) {
        env.output(ButtonCommand.class, this);
    }
}
