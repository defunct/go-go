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
    // TODO Document.
    @Argument
    public String token;

    /**
     * Send some blather to stderr.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
        env.debug(token, "World");
    }
}
