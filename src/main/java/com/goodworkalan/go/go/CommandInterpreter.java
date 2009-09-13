package com.goodworkalan.go.go;

import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public final class CommandInterpreter {
    final TaskFactory taskFactory = new ReflectionTaskFactory();
    
    final Map<String, Responder> commands;

    final Map<Class<? extends Task>, Responder> responders;

    private final Library library;
    
    public CommandInterpreter(String artifactFile) {
        TaskLoader tasks = new TaskLoader(artifactFile);

        this.commands = tasks.commands;
        this.responders = tasks.responders;
        this.library = tasks.library;
        
        for (Responder responder : responders.values()) {
            responder.checkEndlessRecursion(responders, new HashSet<Class<? extends Task>>());
        }
    }
    
    public Library getLibrary() {
        return library;
    }

    public void execute(List<String> arguments) {
        main(arguments.toArray(new String[arguments.size()]));
    }
    
    public void main(String...arguments) {
        execute(arguments);
    }
    
    public void execute(String...arguments) {
        command(arguments).execute();
    }
    
    public CommandPart command(String...arguments) {
        if (arguments.length == 0) {
            throw new GoException(0);
        }
        Responder responder = commands.get(arguments[0]);
        if (responder == null) {
            throw new GoException(0);
        }
        return new CommandPart(this, responder, null).extend(arguments, 1);
    }
}
