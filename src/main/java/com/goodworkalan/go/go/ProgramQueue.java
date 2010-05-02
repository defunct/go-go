package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoError.COMMAND_LINE_NO_ARGUMENTS;
import static com.goodworkalan.go.go.GoError.INVALID_ARGUMENT;
import static com.goodworkalan.go.go.GoError.INVALID_DEFINE_PARAMETER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Queues pseudo-forked Jav-a-Go-Go programs for execution.
 * 
 * @author Alan Gutierrez
 */
public class ProgramQueue {
    private int verbosity = 0;
    
    /** The list of libraries. */
    private final List<File> libraries;

    /** The program arguments. */
    private final String[] arguments;
    
    /** The linked list of programs to run. */
    private final LinkedList<FutureTask<Integer>> programs = new LinkedList<FutureTask<Integer>>();
    
    /** The list of commands available in all libraries. */
    private final Map<List<String>, Artifact> commands;

    /** A monitor to guard the programs list and thread count. */
    private final Object monitor = new Object();
    
    /** The number of threads running. */
    private int threadCount;
    
    public ProgramQueue(List<File> libraries, String...arguments) {
        Map<List<String>, Artifact> commands = new HashMap<List<String>, Artifact>();
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
                                BufferedReader configuration = new BufferedReader(new FileReader(file));
                                String line;
                                while ((line = configuration.readLine()) != null) {
                                    line = line.trim();
                                    if (line.length() == 0 || line.startsWith("#")) {
                                        continue;
                                    }
                                    String[] record = line.split("\\s+", 2);
                                    Artifact artifact = new Artifact(record[0]);
                                    if (record.length > 1) {
                                        for (String path : record[1].split(",")) {
                                            commands.put(Arrays.asList(path.trim().split("\\s+")), artifact);
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
    
        commands.put(Arrays.asList("boot"), new Artifact("com.goodworkalan/go-go"));
        commands.put(Arrays.asList("boot", "hello"), new Artifact("com.goodworkalan/go-go"));
        commands.put(Arrays.asList("boot", "install"), new Artifact("com.goodworkalan/go-go"));
    
        this.libraries = libraries;
        this.arguments = arguments;
        this.commands = commands;
    }

    /**
     * Print the verbose output if the verbose argument has been specified.
     * 
     * @param io
     *            The InputOutput structure.
     * @param message
     *            The message key.
     * @param arguments
     *            The message format arguments.
     */
    void verbose(InputOutput io, String message, Object...arguments) {
        if (verbosity > 0) {
            Environment.error(io, Go.class, message, arguments);
        }
    }

    /**
     * The verbosity of the Jav-a-Go-Go framework.
     * 
     * @return The verbose level.
     */
    public int getVerbosity() {
        return verbosity;
    }

    public int fork(InputOutput io, List<String> arguments) {
        FutureTask<Integer> future = null;
        synchronized (monitor) {
            future = addProgram(new Program(libraries, commands, arguments));
            monitor.notify();
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new GoException(0, e);
        } catch (ExecutionException e) {
            throw new GoException(0, e);
        }
    }
    
    private FutureTask<Integer> addProgram(final Program program) {
        FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() {
                // Exit codes really don't belong here. Put them in GoError.
                // Then return an exception or terminal status.
                int code = 1;
                try {
                    code = program.run(ProgramQueue.this);
                } catch (Throwable e) { 
                    e.printStackTrace();
                }
                synchronized (monitor) {
                    threadCount--;
                    monitor.notify();
                }
                return code;
            }
        });
        programs.add(future);
        return future;
    }

    int start() {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));
        if (args.isEmpty()) {
            throw new GoException(COMMAND_LINE_NO_ARGUMENTS);
        }
        while (!args.isEmpty() && args.getFirst().startsWith("--")) {
            String argument = args.removeFirst();
            if (argument.equals("--verbose")) {
                verbosity++;
            } else if (argument.equals("--no-verbose")) {
                verbosity--;
            } else if (argument.startsWith("--define=")) {
                String define = argument.substring(argument.indexOf('=') + 1);
                String[] definition = define.split(":", 2);
                if (definition.length != 2) {
                    throw new GoError('a', INVALID_DEFINE_PARAMETER, define);
                }
                System.setProperty(definition[0], definition[1]);
            } else {
                throw new GoError('a', INVALID_ARGUMENT, argument);
            }
        }
        FutureTask<Integer> future = addProgram(new Program(libraries, commands, args));
        loop();
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new GoException(0, e);
        } catch (ExecutionException e) {
            throw new GoException(0, e);
        }
    }

    void loop() {
        synchronized (monitor) {
            while (threadCount > 0 || !programs.isEmpty()) {
                if (!programs.isEmpty()) {
                    threadCount++;
                    new Thread(programs.removeFirst()).start();
                }
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    throw new GoException(0, e);
                }
            }
        }
    }
}
