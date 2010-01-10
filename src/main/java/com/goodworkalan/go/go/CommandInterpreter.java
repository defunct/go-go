package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.COMMAND_CLASS_MISSING;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public final class CommandInterpreter {
    final CommandFactory taskFactory = new ReflectionTaskFactory();

    private final ErrorCatcher catcher;

    final ProgramQueue queue;

    final Map<List<String>, Artifact> programs;

    final CommandLoader loader;

    CommandInterpreter(Map<List<String>, Artifact> programs, ProgramQueue queue, ErrorCatcher catcher, List<File> libraries) {
        this.loader = new CommandLoader();

        this.catcher = catcher;
        this.programs = programs;
        this.queue = queue;
    }

    public Library getLibrary() {
        return loader.library;
    }

    /**
     * Execute the given arguments with the command interpreter.
     * 
     * @param dir
     *            The working directory.
     * @param arguments
     *            The arguments to execute.
     * @return The exit code.
     */
    public int execute(String... arguments) {
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
    public int execute(InputOutput io, String... arguments) {
        try {
            command(arguments).execute(io);
        } catch (GoError e) {
            return catcher.inspect(e, io.err, io.out);
        }
        return 0;
    }

    public CommandPart command(String... arguments) {
        if (arguments.length == 0) {
            throw new GoException(0);
        }
        List<String> commandPath = new ArrayList<String>();
        commandPath.add(arguments[0]);

        Artifact artifact = programs.get(commandPath);
        if (artifact != null) {
            loader.addArtifacts(artifact);
        }

        Responder responder = loader.commands.get(arguments[0]);
        if (responder == null) {
            throw new GoException(COMMAND_CLASS_MISSING, arguments[0]);
        }
        return new CommandPart(this, responder, null).extend(arguments, 1);
    }
}
