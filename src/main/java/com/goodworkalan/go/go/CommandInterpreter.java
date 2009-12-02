package com.goodworkalan.go.go;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public final class CommandInterpreter {
    final CommandFactory taskFactory = new ReflectionTaskFactory();
    
    final Map<String, Responder> commands;

    final Map<Class<? extends Commandable>, Responder> responders;

    private final Library library;
    
    private final ErrorCatcher catcher;
    
    public CommandInterpreter(List<Include> transactions) {
        this(transactions.toArray(new Include[transactions.size()]));
    }
    
    public CommandInterpreter(Include...transactions) {
        this(new ErrorCatcher(), transactions);
    }
 
    public CommandInterpreter(ErrorCatcher catcher, Include...transactions) {
        CommandLoader tasks = new CommandLoader(transactions);

        this.catcher = catcher;
        this.commands = tasks.commands;
        this.responders = tasks.responders;
        this.library = tasks.library;
        
        for (Responder responder : responders.values()) {
            responder.checkEndlessRecursion(responders, new HashSet<Class<? extends Commandable>>());
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

    public static void main(String...arguments) {
        System.exit(new ProgramQueue().start(new Program(new File("."), arguments)));
    }
}
