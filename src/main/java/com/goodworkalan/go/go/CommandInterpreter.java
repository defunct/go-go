package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.goodworkalan.go.go.GoException.*;

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

    CommandInterpreter(ProgramQueue queue, ErrorCatcher catcher,
            List<File> libraries) {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        for (File library : libraries) {
            File gogo = new File(library, "go-go");
            if (!(gogo.isDirectory() && gogo.canRead())) {
                continue;
            }
            for (File directory : gogo.listFiles()) {
                if (directory.isDirectory()) {
                    for (File file : directory.listFiles()) {
                        if (file.getName().endsWith(".go")) {
                            try {
                                BufferedReader configuration = new BufferedReader(
                                        new FileReader(file));
                                String line;
                                while ((line = configuration.readLine()) != null) {
                                    line = line.trim();
                                    if (line.length() == 0
                                            || line.startsWith("#")) {
                                        continue;
                                    }
                                    String[] record = line.split("\\s+", 2);
                                    Artifact artifact = new Artifact(record[0]);
                                    if (record.length > 1) {
                                        for (String path : record[1].split(",")) {
                                            programs.put(Arrays.asList(path
                                                    .trim().split("\\s+")),
                                                    artifact);
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                throw new GoException(0, e);
                            }
                        }
                    }
                }
            }
        }

        programs.put(Arrays.asList("go"),
                new Artifact("com.goodworkalan/go-go"));
        programs.put(Arrays.asList("go", "hello"), new Artifact(
                "com.goodworkalan/go-go"));
        programs.put(Arrays.asList("go", "install"), new Artifact(
                "com.goodworkalan/go-go"));
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
