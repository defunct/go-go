package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Environment.flatten;
import static com.goodworkalan.go.go.GoException.*;
import static com.goodworkalan.go.go.GoException.ASSIGNMENT_FAILED;
import static com.goodworkalan.go.go.GoException.CANNOT_CREATE_TASK;
import static com.goodworkalan.go.go.GoException.COMMANDABLE_RESOURCES_IO;
import static com.goodworkalan.go.go.GoException.COMMANDABLE_RESOURCE_IO;
import static com.goodworkalan.go.go.GoException.COMMAND_CLASS_MISSING;
import static com.goodworkalan.go.go.GoException.EXIT;
import static com.goodworkalan.go.go.GoException.FUTURE_EXECUTION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Exclude;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.PathParts;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.ilk.Ilk;
import com.goodworkalan.ilk.Ilk.Box;
import com.goodworkalan.infuse.InfusionException;
import com.goodworkalan.reflective.Reflective;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.retry.Retry;
import com.goodworkalan.utility.Primitives;

/**
 * Executes commands and maintains their environments.
 *
 * @author Alan Gutierrez
 */
public class Executor {
    /** The system verbosity for system standard error messages. */
    private final int systemVerbosity;
    
    /** A map of commands and their arguments to a list of their outputs. */ 
    private final Map<List<String>, CacheEntry> cache = new HashMap<List<String>, CacheEntry>();

    /** The parent executor in the thread that spawned this executor or null. */
    private final Executor parent;

    private final Map<List<String>, Artifact> programs;

    /** A set of keys of artifacts that have already been included. */
    private final Set<Object> seen = new HashSet<Object>();
    
    /** The set of class path URLs that have been inspected for commands. */
    private final Set<URL> urls = new HashSet<URL>();

    /** The Java-a-Go-Go library. */
    private final Library library;
    
    /** The root map of command names to command nodes. */
    private final Map<String, CommandNode> commands = new TreeMap<String, CommandNode>();
    
    /** The map of tasks to command nodes. */
    final Map<Class<? extends Commandable>, CommandNode> commandNodes = new HashMap<Class<? extends Commandable>, CommandNode>();

    /** The map of hidden commands to command nodes. */
    final Map<Class<? extends Commandable>, CommandNode> spawnedCommandNodes = new HashMap<Class<? extends Commandable>, CommandNode>();

    /** The path for the command loader. */
    private final Collection<PathPart> parts = new ArrayList<PathPart>();

    /**
     * Get the output list for the given command key. The output cache is sought
     * by looking first in the cache of the current stack element, then
     * iterating up the stack checking the caches of the ancestor stack
     * elements. When the key is found in the cache of stack element, the output
     * list is returned and the search ends.
     * 
     * @param key
     *            The command key.
     * @return The output list for the command key.
     */
    private CacheEntry getCacheEntry(List<String> key) {
        Executor iterator = this;
        while (iterator != null) {
            if (iterator.cache.containsKey(key)) {
                return iterator.cache.get(key);
            }
            iterator = iterator.parent;
        }
        return null;
    }

    /**
     * Load the tasks found in the libraries specified in the given artifact
     * file. The library dependencies are also loaded.
     * 
     * @param artifactFile
     *            The artifact file.
     */
    Executor(Library library, Map<List<String>, Artifact> programs, int systemVerbosity) {
        seen.add(new Exclude("com.github.bigeasy.danger/danger"));
        seen.add(new Exclude("com.github.bigeasy.verbiage/verbiage"));
        seen.add(new Exclude("com.github.bigeasy.go-go/go-go"));
        seen.add(new Exclude("com.github.bigeasy.infuse/infuse"));
        seen.add(new Exclude("com.github.bigeasy.retry/retry"));
        seen.add(new Exclude("com.github.bigeasy.class-boxer/class-boxer"));
        seen.add(new Exclude("com.github.bigeasy.class-association/class-association"));
        seen.add(new Exclude("com.github.bigeasy.reflective/reflective"));
        seen.add(new Exclude("com.github.bigeasy.reflective/reflective-setter"));
        this.programs = programs;
        this.library = library;
        this.parent = null;
        this.systemVerbosity = systemVerbosity;
    }

    /**
     * Construct a child executor to run in a thread that has an extended class loader.
     * 
     * @param parent The parent executor in the thread that spawned this executor or null. 
     */
    private Executor(Executor parent, Set<Object> seen) {
        this.programs = parent.programs;
        this.seen.addAll(parent.seen);
        this.seen.addAll(seen);
        this.urls.addAll(parent.urls);
        this.parent = parent;
        this.library = parent.library;
        this.commands.putAll(parent.commands);
        this.commandNodes.putAll(parent.commandNodes);
        this.spawnedCommandNodes.putAll(parent.spawnedCommandNodes);
        this.parts.addAll(parent.parts);
        this.cache.putAll(parent.cache);
        this.systemVerbosity = parent.systemVerbosity;
    }

    /**
     * Add a single path part to the class path if it is not already part of the
     * class path. Returns a list containing the given path part if it was not
     * in the class path.
     * 
     * @param pathPart
     *            The path part to add to the class path.
     * @return The collection of path parts not currently in the class path.
     */
    Collection<PathPart> extendClassPath(PathPart pathPart) {
        return extendClassPath(Collections.singleton(pathPart));
    }

    /**
     * Add each of the given path parts to the class path if the path part is
     * not already part of the class path. Returns a list containing the path
     * parts that wrere not already part of the class path.
     * 
     * @param pathPart
     *            The path part to add to the class path.
     * @return The collection of path parts not currently in the class path.
     */
    Collection<PathPart> extendClassPath(Collection<PathPart> pathPart) {
        List<PathPart> unseen = new ArrayList<PathPart>();
        for (PathPart artifact : pathPart) {
            Object key = artifact.getUnversionedKey();
            if (!seen.contains(key)) {
                unseen.add(artifact);
            }
        }
        return unseen;
    }

    /**
     * Read the commandables from the commandable list resources in the class
     * path via the current thread context class loader writing an debugging
     * information or errors to the given I/O bouquet.
     * 
     * @param io
     *            The I/O bouquet.
     */
    private void readConfigurations(InputOutput io) {
        readConfigurations(Thread.currentThread().getContextClassLoader(), io);
    }

    /**
     * Read the commandables from the commandable list resources in the class
     * path via the given class loader writing an debugging information or
     * errors to the given I/O bouquet.
     * 
     * @param io
     *            The I/O bouquet.
     */
    void readConfigurations(ClassLoader classLoader, InputOutput io) {
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.Commandable");
        } catch (IOException e) {
            throw new GoException(COMMANDABLE_RESOURCES_IO, e);
        }
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (!urls.contains(url)) {
                urls.add(url);
                Set<Class<? extends Commandable>> tasks;
                try {
                    tasks = readCommandables(classLoader, url.openStream(), io);
                } catch (IOException e) {
                    throw new GoException(COMMANDABLE_RESOURCE_IO, e, url);
                }

                List<String> classNames = new ArrayList<String>();
                for (Class<? extends Commandable> commandableClass : tasks) {
                    classNames.add(commandableClass.getCanonicalName());
                    if (!commandNodes.containsKey(commandableClass)) {
                        CommandNode commandNode = new CommandNode(io, commandableClass);
                        commandNodes.put(commandableClass, commandNode);
                        Class<? extends Commandable> parentCommandableClass = null;
                        while ((parentCommandableClass = commandNode.getParentCommandClass()) != null) {
                            CommandNode parent = commandNodes.get(parentCommandableClass);
                            if (parent == null) {
                                parent = new CommandNode(io, parentCommandableClass);
                                commandNodes.put(parentCommandableClass, parent);
                            }
                            parent.addCommand(commandNode);
                            commandNode = parent;
                        }
                        // Will reassign sometimes, but that's okay.
                        commands.put(commandNode.getName(), commandNode);
                    }
                }
                debug(io, "readConfiguration", url, classNames);
            }
        }
    }

    /**
     * Read the commandables from the commandable list read from the given input
     * stream, loading them with the given class loader writing an debugging
     * information or errors to the given I/O bouquet.
     * 
     * @param io
     *            The I/O bouquet.
     */

    Set<Class<? extends Commandable>> readCommandables(ClassLoader classLoader, InputStream in, InputOutput io)
    throws IOException {
        BufferedReader lines = new BufferedReader(new InputStreamReader(in));
        Set<Class<? extends Commandable>> commandables = new HashSet<Class<? extends Commandable>>();
        String className;
        while ((className = lines.readLine()) != null) {
            if (className.trim().equals("")) {
                continue;
            }
            Class<?> foundClass;
            try {
                foundClass = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                Environment.error(io, Executor.class, "commandMissing", className);
                continue;
            }
            try {
                commandables.add(foundClass.asSubclass(Commandable.class));
            } catch (ClassCastException e) {
                Environment.error(io, Executor.class, "notCommandable", className, Commandable.class);
            }
        }
        return commandables;
    }

    private Ilk.Box resume(Environment env, CommandNode commandNode, List<String> arguments, int offset, Ilk<?> outcomeType) {
        readConfigurations(env.io);
        if (offset == 0) {
            commandNode = commands.get(arguments.get(0));
            if (commandNode == null) {
                throw new GoException(COMMAND_CLASS_MISSING, arguments.get(offset));
            }
            env.addCommand(arguments.get(0));
            offset++;
        }
        return extend(env, commandNode, arguments, offset, outcomeType);
    }

    private void argument(Environment env, CommandNode commandNode, String name, String value) {
        String command = env.commands.getLast();
        if (name.indexOf(':') == -1) {
            name = command + ':' + name;
        }
        String[] qualified = name.split(":");
        if (qualified.length != 2) {
            throw new GoException(0);
        }

        Class<? extends Commandable> parent = commandNode.getParentCommandClass();
        CommandNode actualCommandNode; 
        if (parent == null) {
            actualCommandNode = commands.get(qualified[0]);
        } else {
            actualCommandNode = commandNodes.get(parent).commands.get(qualified[0]);
        }
        
        if (actualCommandNode == null) {
            throw new GoException(0);
        }

        Assignment assignment = null;
        
        if(qualified[1].equals("verbose")) {
            int index = env.verbosity.size() - 1;
            env.verbosity.set(index, env.verbosity.get(index) + 1);
        } else if (qualified[1].equals("no-verbose")) {
            int index = env.verbosity.size() - 1;
            env.verbosity.set(index, env.verbosity.get(index) - 1);
        } else {
            // Check for a negated boolean flag.
            if (value == null) {
                // FIXME Ensure that there are no arguments beginning with no.
                if (qualified[1].startsWith("no-")) {
                    String negate = qualified[1].substring(3);
                        assignment = actualCommandNode.getAssignments().get(negate); 
                        if (assignment != null) {
                            if (Primitives.box(assignment.setter.getType()).equals(Boolean.class)) {
                                name = actualCommandNode.getName() + ':' + negate;
                                value = "false";
                            } else {
                                assignment = null;
                            }
                        }
                    }
            }
            
            // If the value is not specified, but the type is boolean, then
            // the presence of the argument means true. 
            if (assignment == null) {
                assignment = actualCommandNode.getAssignments().get(qualified[1]); 
                if (assignment == null) {
                    // FIXME Need a much nicer message.
                    throw new GoException(NO_SUCH_ARGUMENT, name);
                }
                if (value == null && Primitives.box(assignment.setter.getType()).equals(Boolean.class)) {
                    value = "true";
                }
            }
            
            try {
                env.conversions.getLast().add(new Conversion(qualified[0], qualified[1], assignment.infuser.infuse(value)));
            } catch (InfusionException e) {
                throw new GoException(0, e);
            }
            env.arguments.getLast().add("--" + qualified[0] + ':' + qualified[1] + '=' + value);
        }
    }
    
    private Ilk.Box extend(Environment env, CommandNode commandNode, List<String> arguments, int offset, Ilk<?> outcomeType) {
        boolean remaining = false;
        for (int i = offset, stop = arguments.size(); i < stop; i++) {
            String argument = arguments.get(i);
            if (remaining) {
                env.remaining.add(argument);
            } else {
                if (argument.equals("--")) {
                    remaining = true;
                } else if (argument.startsWith("--")) {
                    String[] pair = argument.substring(2).split("=", 2);
                    String name = pair[0];
                    String value = pair.length == 1 ? null : pair[1]; 
                    argument(env, commandNode, name, value);
                } else {
                    Artifact artifact = programs.get(flatten(env.commands, argument));
                    if (artifact != null) {
                        Collection<PathPart> unseen = extendClassPath(new ResolutionPart(artifact));
                        if (!unseen.isEmpty()) {
                            return load(unseen, env, commandNode, arguments, i, outcomeType);
                        }
                    }
                    commandNode = commandNode.commands.get(argument);
                    if (commandNode == null) {
                        env.remaining.add(argument);
                        remaining = true;
                    } else {
                        env.addCommand(argument);
                    }
                }
            }
        }
        return execute(env, commands, new CacheEntry(), outcomeType, -1);
    }

    private Ilk.Box loadAfterCommandable(Collection<PathPart> unseen, Environment env, final Map<String, CommandNode> commands, final CacheEntry cacheEntry, final Ilk<?> outcomeType, final int commandIndex) {
        return extendClassPath(unseen, env, new FutureBox() {
            public Box call(Executor exuector, Environment env) {
                return exuector.resumeExecute(env, commands, cacheEntry, outcomeType, commandIndex);
            }
        });
    }
    
    private Ilk.Box resumeExecute(Environment env, Map<String, CommandNode> commands, CacheEntry cacheEntry, Ilk<?> outcomeType, int commandIndex) {
        readConfigurations(env.io);
        return execute(env, commands, cacheEntry, outcomeType, commandIndex);
    }
    
    private Commandable getCommandable(final Class<? extends Commandable> commandableClass) {
        try {
            try {
                return commandableClass.newInstance();
            } catch (Throwable e) {
                throw new ReflectiveException(Reflective.encode(e), e);
            }
        } catch (ReflectiveException e) {
            throw new GoException(CANNOT_CREATE_TASK, e, commandableClass);

        }
    }
    
    private Ilk.Box execute(Environment env, Map<String, CommandNode> commands, CacheEntry cacheEntry, Ilk<?> outcomeType, int commandIndex) {
        // Run any hidden commands.
        for (;;) {
            while (!env.commandables.isEmpty()) {
                List<Class<? extends Commandable>> transients = new ArrayList<Class<? extends Commandable>>(env.commandables);

                Class<? extends Commandable> commandableClass = env.commandables.removeFirst();

                CommandNode commandNode = commandNodes.get(commandableClass);
                
                if (commandNode == null) {
                    commandNode = spawnedCommandNodes.get(commandableClass);
                    if (commandNode == null) {
                        commandNode = new CommandNode(env.io, commandableClass);
                        spawnedCommandNodes.put(commandableClass, commandNode);
                    }
                }

                Commandable commandable = getCommandable(commandableClass);
                
                if (commandNode != null) {
                    // Set the arguments for the command.
                    for (Conversion conversion : env.conversions.get(commandIndex)) {
                        if (conversion.command.equals(env.commands.get(commandIndex))) {
                            Assignment assignment = commandNode.getAssignments().get(conversion.name);
                            if (assignment != null) {
                                try {
                                    assignment.setter.set(commandable, conversion.value);
                                } catch (ReflectiveException e) {
                                    if (e.getCode() == Reflective.INVOCATION_TARGET) {
                                        throw new GoException(ASSIGNMENT_EXCEPTION_THROWN, e, commandable.getClass().getCanonicalName(), assignment.setter.getNative().getName());
                                    }
                                    throw new GoException(ASSIGNMENT_FAILED, e, commandable.getClass().getCanonicalName(), assignment.setter.getNative().getName());
                                }
                            }
                        }
                    }
                }
                
                
                // Create sub environment.
                Environment subEnv = new Environment(env, commandable.getClass(), commandIndex);
                
                boolean terminate = false;
                
                Command command = commandable.getClass().getAnnotation(Command.class);
                boolean cached = command == null || command.cache();

                // Execute the command.
                try {
                    commandable.execute(subEnv);
                } catch (Exit exit) {
                    if (exit.code != 0) {
                        throw new GoException(EXIT, exit);
                    }
                    terminate = true;
                }
                if (cached) {
                    if (!cacheEntry.transients.isEmpty()) {
                        throw new GoException(0, commandable.getClass());
                    }
                    cacheEntry.outputs.addAll(subEnv.outputs.get(commandIndex));
                    if (cacheEntry.outputs.isEmpty()) {
                        Ilk.Box box = new Ilk<IgnorableOutput>(IgnorableOutput.class).box(new IgnorableOutput());
                        cacheEntry.outputs.add(box);
                    }
                } else if (cacheEntry.transients.isEmpty()) {
                    cacheEntry.transients.addAll(transients);
                }
                if (terminate) {
                    return chooseBox(env, outcomeType);
                }
                if (!subEnv.pathParts.isEmpty()) {
                    Collection<PathPart> unseen = extendClassPath(subEnv.pathParts);
                    if (!unseen.isEmpty()) {
                        return loadAfterCommandable(unseen, env, commands, cacheEntry, outcomeType, commandIndex);
                    }
                }
            }
            
            if (!cacheEntry.outputs.isEmpty()) {
                cache.put(env.getCommandKey(0, commandIndex + 1), cacheEntry);
            }
            
            commandIndex++;
            
            if (commandIndex == env.commands.size()) {
                break;
            }
            
            CacheEntry cached = getCacheEntry(env.getCommandKey(0, commandIndex + 1));

            // Find the CommandNode.
            CommandNode commandNode = commands.get(env.commands.get(commandIndex));
            
            // Descend the command tree.
            commands = commandNode.commands;

            // Tree list will sort the keys by assignability.
            env.outputs.add(new ArrayList<Ilk.Box>());

            if (cached != null) {
                env.outputs.get(commandIndex).addAll(cached.outputs);
                env.commandables.addAll(cached.transients);
            } else {
                cacheEntry = new CacheEntry();
                env.commandables.add(commandNode.getCommandClass());
            }
        }
        
        return chooseBox(env, outcomeType);
    }

    /**
     * Get the boxed command output form the the given environment that is
     * assignable from the type indicated by the given super type token.
     * 
     * @param env
     *            The environment.
     * @param ilk
     *            The super type token.
     * @return The boxed command output assignable to the given type or null if
     *         none is found.
     */
    public Ilk.Box chooseBox(Environment env, Ilk<?> ilk) {
        if (ilk != null) {
            return env.get(ilk.key, env.outputs.size() - 1);
        }
        return null;
    }

    private Ilk.Box extendClassPath(Collection<PathPart> unseen, Environment env, final FutureBox box) {
        Collection<PathPart> subPath = new ArrayList<PathPart>();
        subPath = library.resolve(unseen, seen);
        Set<Object> subSeen = new HashSet<Object>();
        for (PathPart part : subPath) {
            subSeen.add(part.getUnversionedKey());
        }
        final Executor childExecutor = new Executor(this, subSeen);
        final Environment childEnv = new Environment(env, childExecutor);
        FutureTask<Ilk.Box> future = new FutureTask<Ilk.Box>(new Callable<Ilk.Box>() {
            public Ilk.Box call() {
                return box.call(childExecutor, childEnv);
            }
        });
        final Thread thread = new Thread(future);
        thread.setContextClassLoader(PathParts.getClassLoader(subPath, Thread.currentThread().getContextClassLoader()));
        thread.start();
        Retry.retry(new Retry.Procedure() {
            public void retry() throws InterruptedException {
                thread.join();
            }
        });
        try {
            return Retry.retry(future);
        } catch (ExecutionException e) {
            throw new GoException(FUTURE_EXECUTION, e);
        }
    }
    
    private interface FutureBox {
        public Ilk.Box call(Executor exuector, Environment env);
    }

    private Ilk.Box load(Collection<PathPart> unseen, Environment env, final CommandNode commandNode, final List<String> arguments, final int offset, final Ilk<?> outcomeType) {
        return extendClassPath(unseen, env, new FutureBox() {
            public Box call(Executor executor, Environment env) {
                return executor.resume(env, commandNode, arguments, offset, outcomeType);
            }
        });
    }

    Ilk.Box start(InputOutput io, List<String> arguments, Ilk<?> outcomeType) {
        Environment env = new Environment(library, io, this);
        readConfigurations(env.io);
        Artifact artifact = programs.get(flatten(arguments.get(0)));
        if (artifact != null) {
            Collection<PathPart> unseen = extendClassPath(new ResolutionPart(artifact));
            if (!unseen.isEmpty()) {
                return load(unseen, env, null, arguments, 0, outcomeType);
            }
        }
        return resume(env, null, arguments, 0, outcomeType);
    }

    // FIXME Maybe this is execute and call doesn't do any unwrapping?
    public int run(InputOutput io, List<String> arguments) {
        try {
            start(io, arguments, null);
            return 0;
        } catch (GoException e) {
            // FIXME Can this even be reached? Add program in program queue
            // wraps as well. Yes, it can be reached because this is not a fork,
            // and therefore not a program. Do I want to catch it or let it
            // propagate out to the program queue?
            return e.unwrap(io, systemVerbosity);
        }
    }

    public int run(InputOutput io, Object...arguments) {
        return run(io, flatten(arguments));
    }

    public <T> T run(Class<T> outcomeType, InputOutput io, Object...arguments) {
        return run(new Ilk<T>(outcomeType), io, flatten(arguments));
    }
    
    public <T> T run(Ilk<T> outcomeType, InputOutput io, Object...arguments) {
        return run(outcomeType, io, flatten(arguments));
    }
    
    public <T> T run(Class<T> outcomeType, InputOutput io, List<String> arguments) {
        return run(new Ilk<T>(outcomeType), io, flatten(arguments));
    }

    public <T> T run(Ilk<T> outcomeType, InputOutput io, List<String> arguments) {
        Ilk.Box outcome = start(io, arguments, outcomeType);
        if (outcome == null) {
            return null;
        }
        return outcome.cast(outcomeType);
    }


    public void fork(InputOutput io, Object...arguments) {
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
    private void debug(InputOutput io, String message, Object...arguments) {
        if (systemVerbosity > 1) {
            Environment.error(io, Executor.class, message, arguments);
        }
    }

}
