package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoError.COMMAND_LINE_NO_ARGUMENTS;
import static com.goodworkalan.go.go.GoError.INVALID_ARGUMENT;
import static com.goodworkalan.go.go.GoError.INVALID_DEFINE_PARAMETER;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    
    /** A monitor to guard the programs list and thread count. */
    private final Object monitor = new Object();
    
    /** The number of threads running. */
    private int threadCount;
    
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

    public ProgramQueue(List<File> libraries, String...arguments) {
        this.libraries = libraries;
        this.arguments = arguments;
    }
    
    public int fork(InputOutput io, List<String> arguments) {
        FutureTask<Integer> future = null;
        synchronized (monitor) {
            future = addProgram(new Program(libraries, arguments));
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
        FutureTask<Integer> future = addProgram(new Program(libraries, args));
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
