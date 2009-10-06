package com.goodworkalan.go.go;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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
        execute(arguments.toArray(new String[arguments.size()]));
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

    public static void main(String...arguments) throws Exception {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));
        if (args.isEmpty()) {
            throw new GoException(0);
        }
        String artifacts = null;
        boolean debug = false;
        for (;;) {
            artifacts = args.removeFirst();
            if (!artifacts.startsWith("--")) {
                break;
            }
            if (artifacts.equals("--debug")) {
                debug = true;
            } else {
                throw new GoException(0);
            }
        }
        CommandInterpreter ci = new CommandInterpreter(artifacts);
        File file = new File(artifacts);
        try {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".bat")) {
                name = name.substring(0, name.length() - 4);
            }
            ci.command(file.getName());
            args.addFirst(file.getName());
        } catch (GoException e) {
        }
        if (debug) {
            System.out.println(args);
        }
        ci.execute(args);
        if (debug) {
            System.out.printf("%.2fM/%.2fM\n", 
                    (double) Runtime.getRuntime().totalMemory() /  1024 / 1024,
                    (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
        }
    }
}
