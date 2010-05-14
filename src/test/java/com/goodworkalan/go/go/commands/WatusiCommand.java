package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

@Command(parent = Snap.class)
public class WatusiCommand implements Commandable {
    private String repeat;
    
    @Argument
    public void addRepeat(String repeat) {
        this.repeat = repeat;
    }

    public void execute(Environment env) {
        env.output(String.class, repeat);
    }
}
