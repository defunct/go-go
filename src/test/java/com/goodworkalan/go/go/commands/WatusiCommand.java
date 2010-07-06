package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

// TODO Document.
@Command(parent = Snap.class)
public class WatusiCommand implements Commandable {
    // TODO Document.
    private String repeat;
    
    // TODO Document.
    @Argument
    public void addRepeat(String repeat) {
        this.repeat = repeat;
    }

    // TODO Document.
    public void execute(Environment env) {
        env.output(String.class, repeat);
    }
}
