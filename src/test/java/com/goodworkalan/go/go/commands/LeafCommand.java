package com.goodworkalan.go.go.commands;

import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;

@Command(parent = BranchCommand.class)
public class LeafCommand implements Commandable {
    public void execute(Environment env) {
    }
}
