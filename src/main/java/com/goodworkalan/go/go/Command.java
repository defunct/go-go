package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command line, so that we can build command lines in Java and not
 * have to define both command line interfaces and Java interfaces for some core
 * objects.
 * 
 * @author Alan Gutierrez
 */
public class Command extends ArrayList<Parameter> {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;
    
    /** The list of commands. */
    private final String name;

    /**
     * Create a command.
     * 
     * @param name
     *            The name of the command.
     */
    public Command(String name) {
        this.name = name;
    }
    
    public Command(Command command) {
        super(command);
        this.name = command.name;
    }
    
    public Command(String name, Command command) {
        super(command);
        this.name = name;
    }

    public void add(String name, String value) {
        add(new Parameter(name, value));
    }

    public Parameter getFirst(String name) {
        for (Parameter parameter : this) {
            if (parameter.matches(this.name, name)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Convert a list of commands into command line arguments.
     * 
     * @param commandLine
     *            A list of commands.
     * @return An array of string commands.
     */
    public static String[] toArguments(List<Command> commandLine) {
        List<String> arguments = new ArrayList<String>();
        for (Command command : commandLine) {
            arguments.add(command.name);
            for (Parameter parameter : command) {
                arguments.add(parameter.toString());
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }
}
