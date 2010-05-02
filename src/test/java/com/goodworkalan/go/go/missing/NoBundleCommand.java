package com.goodworkalan.go.go.missing;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.commands.Snap;

/**
 * Test a missing message bundle.
 *
 * @author Alan Gutierrez
 */
@Command(parent = Snap.class)
public class NoBundleCommand implements Commandable {
    /**
     * Send a message to stderr.
     * 
     * @param env
     *            The environment.
     */
    public void execute(Environment env) {
        env.verbose("a");
    }
}
