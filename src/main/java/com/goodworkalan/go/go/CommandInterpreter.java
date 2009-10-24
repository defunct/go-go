package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
    
    private final ErrorCatcher catcher;
    
    public CommandInterpreter(List<Include> transactions) {
        this(transactions.toArray(new Include[transactions.size()]));
    }
    
    public CommandInterpreter(Include...transactions) {
        this(new ErrorCatcher(), transactions);
    }
 
    public CommandInterpreter(ErrorCatcher catcher, Include...transactions) {
        TaskLoader tasks = new TaskLoader(transactions);

        this.catcher = catcher;
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

    /**
     * Execute the given arguments with the command interpreter.
     * 
     * @param arguments
     *            The arguments to execute.
     * @return The exit code.
     */
    public int execute(String...arguments) {
        return execute(new InputOutput(), arguments);
    }

    /**
     * Execute the given arguments with the command interpreter using the given
     * input/output streams.
     * 
     * @param io
     *            The input/output streams.
     * @param arguments
     *            The arguments to execute.
     * @return The exit code.
     */
    public int execute(InputOutput io, List<String> arguments) {
        return execute(io, arguments.toArray(new String[arguments.size()]));
    }
    
    /**
     * Execute the given arguments with the command interpreter using the given
     * input/output streams.
     * 
     * @param io
     *            The input/output streams.
     * @param arguments
     *            The arguments to execute.
     * @return The exit code.
     */
    public int execute(InputOutput io, String...arguments) {
        try {
            command(arguments).execute(io);
        } catch (GoError e) {
            return catcher.inspect(e, io.err, io.out);
        }
        return 0;
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

    // FIXME Don't throw Exception.
    public static void main(String...arguments) throws Exception {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));
        if (args.isEmpty()) {
            throw new GoException(0);
        }
        List<Include> includes = new ArrayList<Include>();
        boolean debug = false;
        for (Iterator<String> each = args.iterator(); each.hasNext();) {
            String argument = each.next();
            if (argument.equals("--")) {
                break;
            } else if (argument.startsWith("--go:")) {
                if (argument.equals("--go:debug")) {
                    debug = true;
                } else if (argument.equals("--go:no-debug")) {
                    debug = false;
                } else if (argument.startsWith("--go:artifacts=")) {
                    File artifacts = new File(argument.substring(argument.indexOf('=') + 1));
                    if (artifacts.exists()) {
                        includes.addAll(Artifacts.read(artifacts));
                    }
                } else if (argument.startsWith("--go:define=")) {
                    String[] definition = argument.substring(argument.indexOf('=') + 1).split(":", 2);
                    if (definition.length != 2) {
                        throw new GoError(0);
                    }
                    System.out.println(definition[0] + "=" + definition[1]);
                    System.setProperty(definition[0], definition[1]);
                } else {
                    throw new GoException(0);
                }
                each.remove();
            }
        }
        File artifacts = new File(args.removeFirst());
        BufferedReader configuration = new BufferedReader(new FileReader(artifacts));
        String line;
        while ((line = configuration.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("@")) {
                String[] argument = line.split("\\s+", 3);
                if (argument.length == 1) {
                    throw new GoException(0);
                }
                if (argument[1].equals("debug")) {
                    debug = true;
                } else if (argument[1].equals("no-debug")) {
                    debug = false;
                } else if (argument[1].equals("artifacts")) {
                    File additional = new File(argument[2]);
                    if (additional.exists()) {
                        includes.addAll(Artifacts.read(additional));
                    }
                } else {
                    throw new GoException(0);
                }
            }
        }
        configuration.close();
        includes.addAll(Artifacts.read(artifacts));
        CommandInterpreter ci = new CommandInterpreter(includes);
        try {
            String name = artifacts.getName().toLowerCase();
            if (name.endsWith(".bat")) {
                name = name.substring(0, name.length() - 4);
            }
            ci.command(artifacts.getName());
            args.addFirst(artifacts.getName());
        } catch (GoException e) {
        }
        if (debug) {
            System.out.println(args);
        }
        int code = ci.execute(new InputOutput(), args);
        if (debug) {
            System.out.printf("%.2fM/%.2fM\n", 
                    (double) Runtime.getRuntime().totalMemory() /  1024 / 1024,
                    (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
        }
        System.exit(code);
    }
}
