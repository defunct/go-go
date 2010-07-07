package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

/**
 * Test output messages.
 * 
 * @author Alan Gutierrez
 */
@Command(parent = Snap.class)
public class PrattleCommand implements Commandable {
    /** The debugging message code. */
    @Argument
    public String code;

    /**
     * Send some blather to stderr.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
        env.debug(code, "World");
    }
}
