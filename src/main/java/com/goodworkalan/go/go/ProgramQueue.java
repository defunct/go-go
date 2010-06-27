package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoError.COMMAND_LINE_NO_ARGUMENTS;
import static com.goodworkalan.go.go.GoError.INVALID_ARGUMENT;
import static com.goodworkalan.go.go.GoError.INVALID_DEFINE_PARAMETER;
import static com.goodworkalan.go.go.GoException.FUTURE_EXECUTION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.retry.Retry;

/**
 * Queues pseudo-forked Jav-a-Go-Go programs for execution.
 * 
 * @author Alan Gutierrez
 */
class ProgramQueue {
    private int verbosity = 0;
    
    /** The list of libraries. */
    private final List<File> libraries;

    /** The program arguments. */
    private final List<String> arguments;
    
    /** The linked list of programs to run. */
    private final LinkedList<FutureTask<Integer>> executions = new LinkedList<FutureTask<Integer>>();
    
    /** The list of commands available in all libraries. */
    private final Map<List<String>, Artifact> programs;

    /** A monitor to guard the programs list and thread count. */
    private final Object monitor = new Object();
    
    /** The number of threads running. */
    private int threadCount;
    
    public ProgramQueue(List<File> libraries, String...arguments) {
        LinkedList<String> args = new LinkedList<String>(Arrays.asList(arguments));
        if (args.isEmpty()) {
            // FIXME Should be an error.
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
                    throw new GoError(INVALID_DEFINE_PARAMETER, define);
                }
                System.setProperty(definition[0], definition[1]);
            } else {
                throw new GoError(INVALID_ARGUMENT, argument);
            }
        }
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
                                        List<List<String>> found = new ArrayList<List<String>>(); 
                                        for (String path : record[1].split(",")) {
                                            List<String> command = Arrays.asList(path.trim().split("\\s+"));
                                            commands.put(command, artifact);
                                            found.add(command);
                                        }
                                        debug(new InputOutput(), "programsFound", artifact, found);
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
    
        commands.put(Arrays.asList("boot"), new Artifact("com.github.bigeasy.go-go/go-go/0.1.4"));
        commands.put(Arrays.asList("boot", "hello"), new Artifact("com.github.bigeasy.go-go/go-go/0.1.4"));
        commands.put(Arrays.asList("boot", "install"), new Artifact("com.github.bigeasy.go-go/go-go/0.1.4"));
    
        this.libraries = libraries;
        this.arguments = new ArrayList<String>(args);
        this.programs = commands;
    }
    

    /**
     * Print the debug output if the verbose argument has been specified twice.
     * 
     * @param io
     *            The InputOutput structure.
     * @param message
     *            The message key.
     * @param arguments
     *            The message format arguments.
     */
    private void debug(InputOutput io, String message, Object...arguments) {
        if (verbosity > 1) {
            Environment.error(io, Go.class, message, arguments);
        }
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
    private void verbose(InputOutput io, String message, Object...arguments) {
        if (verbosity > 0) {
            Environment.error(io, Go.class, message, arguments);
        }
    }

    public int fork(InputOutput io, List<String> arguments) {
        FutureTask<Integer> future = null;
        synchronized (monitor) {
            future = addProgram(io, arguments);
            monitor.notify();
        }
        try {
            return Retry.retry(future);
        } catch (ExecutionException e) {
            throw new GoException(FUTURE_EXECUTION, e);
        }
    }

    /**
     * Run the given program and return zero if the execution is successful.
     * <p>
     * Because method will only ever return zero, the program thread doesn't
     * really need to be implemented as a future. We could create a
     * <code>Runnable</code> that catches an exception and we can inspect that
     * result to determine if we need to throw an exception. All this would be
     * duplicating the exception propagation code of <code>FutureTask</code> and
     * we'd have to add a count down latch, so here we're using
     * <code>FutureTask</code> for its exception propagation code and as a count
     * down latch, since it would be cumbersome to write our own.
     * 
     * @param io
     *            The input/output bouquet.
     * @param arguments
     *            The command line arguments.
     * @return Always returns zero, not sure why it is returning anything.
     */
    private int runProgram(InputOutput io, List<String> arguments) {
        Executor executor = new Executor(new Library(libraries.toArray(new File[libraries.size()])), programs,  verbosity);
        verbose(io, "start", arguments);
        long start = System.currentTimeMillis();
        try {
            executor.start(io, arguments, null);
        } finally {
            verbose(io, "stop",
                    System.currentTimeMillis() - start,
                    (double) Runtime.getRuntime().totalMemory() / 1024 / 1024,
                    (double) Runtime.getRuntime().freeMemory() / 1024 / 1024);
            synchronized (monitor) {
                threadCount--;
                monitor.notify();
            }
        }
        return 0;
    }
    
    private FutureTask<Integer> addProgram(final InputOutput io, final List<String> arguments) {
        FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() {
                return runProgram(io, arguments);
            }
        });
        executions.add(future);
        return future;
    }

    private int start(InputOutput io) {
        FutureTask<Integer> future = addProgram(io, arguments);
        loop();
        int code;
        try {
            code = Retry.retry(future);
        } catch (ExecutionException e) {
            // FIXME Unpack here?
            throw new GoException(FUTURE_EXECUTION, e);
        }
        return code;
    }

    public int run(InputOutput io) {
        try {
            return start(io);
        } catch (GoException e) {
            return e.unwrap(io, verbosity);
        }
    }

    private void loop() {
        synchronized (monitor) {
            while (threadCount > 0 || !executions.isEmpty()) {
                if (!executions.isEmpty()) {
                    threadCount++;
                    new Thread(executions.removeFirst()).start();
                }
                Retry.retry(new Retry.Procedure() {
                    public void retry() throws InterruptedException {
                        monitor.wait();
                    }
                });
            }
        }
    }
}
