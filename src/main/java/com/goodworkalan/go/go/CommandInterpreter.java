package com.goodworkalan.go.go;

import java.io.File;
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
    
    public CommandInterpreter(List<Transaction> transactions) {
        this(transactions.toArray(new Transaction[transactions.size()]));
    }
 
    public CommandInterpreter(Transaction...transactions) {
        TaskLoader tasks = new TaskLoader(transactions);

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
        List<Transaction> transactions = new ArrayList<Transaction>();
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
                        transactions.add(Artifacts.read(artifacts));
                    }
                } else {
                    throw new GoException(0);
                }
                each.remove();
            }
        }
        File artifacts = new File(args.removeFirst());
        transactions.add(Artifacts.read(artifacts));
        CommandInterpreter ci = new CommandInterpreter(transactions);
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
        ci.execute(args);
        if (debug) {
            System.out.printf("%.2fM/%.2fM\n", 
                    (double) Runtime.getRuntime().totalMemory() /  1024 / 1024,
                    (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
        }
    }
}
