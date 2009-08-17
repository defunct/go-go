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
public class Command {
    /** The list of commands. */
    private final String name;

    /** The list of parameters. */
    private List<Parameter> parameters = new ArrayList<Parameter>();
    
    /** The next command. */
    private Command next;

    /**
     * Create a command.
     * 
     * @param name
     *            The name of the command.
     */
    public Command(String name) {
        this.name = name;
    }

    /**
     * Add a command to the list of commands.
     * 
     * @param command
     *            The command to append to the list of commands.
     */
    public void addCommand(Command command) {
        if (next == null ) {
            next = command;
        } else {
            next.addCommand(command);
        }
    }

    /**
     * Add the string representation of the object to list of parameters.
     * 
     * @param name
     *            The object name.
     * @param value
     *            The object value.
     */
    public void addParameter(String name, Object value) {
        parameters.add(new Parameter(name, value.toString()));
    }
    
    private void toArguments(List<String> arguments, Command command) {
        arguments.add(command.name);
        for (Parameter parameter : command.parameters) {
            arguments.add(parameter.toString());
        }
        if (command.next != null) {
            toArguments(arguments, command.next);
        }
    }
    
    public String[] toArguments() {
        List<String> arguments = new ArrayList<String>();
        toArguments(arguments, this);
        return arguments.toArray(new String[arguments.size()]);
    }
}
