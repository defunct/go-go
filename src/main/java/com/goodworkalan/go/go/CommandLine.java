package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.List;

public class CommandLine extends ArrayList<Command> {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Create an empty command line.
     */
    public CommandLine() {
    }

    /**
     * Create a copy of the given command line.
     * @param commandLine
     */
    public CommandLine(List<Command> commandLine) {
        for (int i = 0, size = commandLine.size(); i < size; i++) {
            add(new Command(commandLine.get(i)));
        }
    }

    public CommandLine subCommandLine(int fromIndex, int toIndex) {
        return new CommandLine(subList(fromIndex, toIndex));
    }

    public CommandLine rename(int index, String name) {
        CommandLine copy = new CommandLine(this);
        copy.set(index, new Command(name, get(index)));
        return new CommandLine(copy);
    }
}
