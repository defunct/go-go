package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.ASSIGNMENT_EXCEPTION_THROWN;
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
import java.util.concurrent.ThreadPoolExecutor;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Exclude;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.infuse.InfusionException;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;
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
    
    /** Used to construct commands. */
    private final ReflectiveFactory reflective;
    
    /**
     * The thread factory that extends the class path by setting the context
     * class loader with a class loader built from a path part collection.
     */
    private final ProgramThreadFactory threadFactory;
    
    /** An executor service. */
    private final ThreadPoolExecutor threadPool;
    
    /** A map of commands and their arguments to a list of their outputs. */ 
    private final Map<List<List<String>>, List<Object>> cache = new HashMap<List<List<String>>, List<Object>>();

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
    List<Object> getCache(List<List<String>> key) {
        Executor iterator = this;
        while (iterator != null) {
            if (iterator.cache.containsKey(key)) {
                return iterator.cache.get(key);
            }
            iterator = iterator.parent;
        }
        return Collections.emptyList();
    }
    /**
     * Load the tasks found in the libraries specified in the given artifact
     * file. The library dependencies are also loaded.
     * 
     * @param artifactFile
     *            The artifact file.
     */
    Executor(ReflectiveFactory reflective, Library library, Map<List<String>, Artifact> programs, ProgramThreadFactory threadFactory, ThreadPoolExecutor threadPool, int systemVerbosity) {
        seen.add(new Exclude("com.github.bigeasy.danger/danger"));
        seen.add(new Exclude("com.github.bigeasy.verbiage/verbiage"));
        seen.add(new Exclude("com.github.bigeasy.go-go/go-go"));
        seen.add(new Exclude("com.github.bigeasy.infuse/infuse"));
        seen.add(new Exclude("com.github.bigeasy.retry/retry"));
        seen.add(new Exclude("com.github.bigeasy.class-boxer/class-boxer"));
        seen.add(new Exclude("com.github.bigeasy.class-association/class-association"));
        seen.add(new Exclude("com.github.bigeasy.reflective/reflective"));
        this.programs = programs;
        this.reflective = reflective;
        this.library = library;
        this.parent = null;
        this.threadFactory = threadFactory;
        this.threadPool = threadPool;
        this.systemVerbosity = systemVerbosity;
    }

    /**
     * Construct a child executor to run in a thread that has an extended class loader.
     * 
     * @param parent The parent executor in the thread that spawned this executor or null. 
     */
    private Executor(Executor parent) {
        this.programs = parent.programs;
        this.reflective = parent.reflective;
        this.seen.addAll(parent.seen);
        this.urls.addAll(parent.urls);
        this.parent = parent;
        this.library = parent.library;
        this.commands.putAll(parent.commands);
        this.commandNodes.putAll(parent.commandNodes);
        this.parts.addAll(parent.parts);
        this.threadFactory = parent.threadFactory;
        this.threadPool = parent.threadPool;
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
    Collection<PathPart> extendClassPath(Collection<PathPart> artifacts) {
        List<PathPart> unseen = new ArrayList<PathPart>();
        for (PathPart artifact : artifacts) {
            Object key = artifact.getUnversionedKey();
            if (!seen.contains(key)) {
                unseen.add(artifact);
            }
        }
        Collection<PathPart> subPath = new ArrayList<PathPart>();
        if (!unseen.isEmpty()) {
            subPath = library.resolve(unseen, seen);
            for (PathPart part : subPath) {
                seen.add(part.getUnversionedKey());
            }
        }
        return subPath;
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
                for (Class<? extends Commandable> taskClass : tasks) {
                    classNames.add(taskClass.getCanonicalName());
                    if (!commandNodes.containsKey(taskClass)) {
                        CommandNode CommandNode = new CommandNode(taskClass);
                        commandNodes.put(taskClass, CommandNode);
                        Class<? extends Commandable> parentTaskClass = null;
                        while ((parentTaskClass = CommandNode.getParentCommandClass()) != null) {
                            CommandNode parent = commandNodes.get(parentTaskClass);
                            if (parent == null) {
                                parent = new CommandNode(parentTaskClass);
                                commandNodes.put(parentTaskClass, parent);
                            }
                            parent.addCommand(CommandNode);
                            CommandNode = parent;
                        }
                        // Will reassign sometimes, but that's okay.
                        commands.put(CommandNode.getName(), CommandNode);
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
            if (Commandable.class.isAssignableFrom(foundClass)) {
                commandables.add(taskClass(foundClass));
            } else {
                Environment.error(io, Executor.class, "notCommandable", className, Commandable.class);
            }
        }
        return commandables;
    }

    private Outcome resume(Environment env, CommandNode CommandNode, List<String> arguments, int offset, Class<?> outcomeClass) {
        readConfigurations(env.io);
        if (offset == 0) {
            CommandNode = commands.get(arguments.get(0));
            if (CommandNode == null) {
                throw new GoException(COMMAND_CLASS_MISSING, arguments.get(offset));
            }
            env.addCommand(arguments.get(0));
            offset++;
        }
        return extend(env, CommandNode, arguments, offset, outcomeClass);
    }

    private void argument(Environment env, CommandNode CommandNode, String name, String value) {
        String command = env.commands.getLast();
        if (name.indexOf(':') == -1) {
            name = command + ':' + name;
        }
        String[] qualified = name.split(":");
        if (qualified.length != 2) {
            throw new GoException(0);
        }

        Class<? extends Commandable> parent = CommandNode.getParentCommandClass();
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
                    throw new GoException(0);
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
    
    private Outcome extend(Environment env, CommandNode CommandNode, List<String> arguments, int offset, Class<?> outcomeClass) {
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
                    argument(env, CommandNode, name, value);
                } else {
                    Artifact artifact = programs.get(flatten(env.commands, argument));
                    if (artifact != null) {
                        Collection<PathPart> unseen = extendClassPath(new ResolutionPart(artifact));
                        if (!unseen.isEmpty()) {
                            return load(unseen, env, CommandNode, arguments, i, outcomeClass);
                        }
                    }
                    CommandNode = CommandNode.commands.get(argument);
                    if (CommandNode == null) {
                        env.remaining.add(argument);
                        remaining = true;
                    } else {
                        env.addCommand(argument);
                    }
                }
            }
        }
        return execute(env, commands, outcomeClass, 0);
    }

    private Outcome loadAfterCommandable(Collection<PathPart> parts, Environment env, final Map<String, CommandNode> commands, final Class<?> outcomeClass, final int offset) {
        final Executor childExecutor = new Executor(this);
        final Environment childEnv = new Environment(env, childExecutor);
        FutureTask<Outcome> future = new FutureTask<Outcome>(new Callable<Outcome>() {
            public Outcome call() {
                return childExecutor.resumeExecute(childEnv, commands, outcomeClass, offset);
            }
        });
        return waitForOutcome(parts, future);
    }
    
    private Outcome resumeExecute(Environment env, Map<String, CommandNode> commands, Class<?> outcomeClass, int offset) {
        readConfigurations(env.io);
        return execute(env, commands, outcomeClass, offset);
    }

    private Outcome execute(Environment env, Map<String, CommandNode> commands, Class<?> outcomeClass, int offset) {
        // Run any hidden commands.
        while (!env.hiddenCommands.isEmpty()) {
            Commandable commandable = env.hiddenCommands.removeFirst();

            // Create sub environment.
            Environment subEnv = new Environment(env, commandable.getClass(), offset);

            // Execute the command.
            try {
                commandable.execute(subEnv);
            } catch (Exit exit) {
                throw new GoException(EXIT, exit);
            }
            
            env.parentOutputs.get(env.parentOutputs.size() - 1).addAll(subEnv.outputs);
            
            if (!subEnv.pathParts.isEmpty()) {
                Collection<PathPart> unseen = extendClassPath(subEnv.pathParts);
                if (!unseen.isEmpty()) {
                    return loadAfterCommandable(unseen, env, commands, outcomeClass, offset);
                }
            }
        }
        // Go through each command executing if it is not cached.
        for (int i = offset, stop = env.commands.size(); i < stop; i++) {
            // Find the CommandNode.
            CommandNode CommandNode = commands.get(env.commands.get(i));
            
            // Set up the lookup of a child CommandNode.
            commands = CommandNode.commands;
            
            // Create an instance of the commandable.
            Commandable commandable;
            try {
                commandable = reflective.newInstance(CommandNode.getCommandClass());
            } catch (ReflectiveException e) {
                throw new GoException(CANNOT_CREATE_TASK, e, CommandNode.getCommandClass().getCanonicalName());
            }
            
            // Set the arguments for the command.
            for (Conversion conversion : env.conversions.get(i)) {
                if (conversion.command.equals(CommandNode.getName())) {
                    Assignment assignment = CommandNode.getAssignments().get(conversion.name);
                    try {
                        assignment.setter.set(commandable, conversion.value);
                    } catch (ReflectiveException e) {
                        if (e.getCode() == ReflectiveException.INVOCATION_TARGET) {
                            throw new GoException(ASSIGNMENT_EXCEPTION_THROWN, e, commandable.getClass().getCanonicalName(), assignment.setter.getNative().getName());
                        }
                        throw new GoException(ASSIGNMENT_FAILED, e, commandable.getClass().getCanonicalName(), assignment.setter.getNative().getName());
                    }
                }
            }
            
            // Create sub environment.
            Environment subEnv = new Environment(env, commandable.getClass(), i + 1);
            
            // Execute the command.
            try {
                commandable.execute(subEnv);
            } catch (Exit exit) {
                throw new GoException(EXIT, exit);
            }
            
            env.parentOutputs.add(subEnv.outputs);
            if (!subEnv.pathParts.isEmpty()) {
                Collection<PathPart> unseen = extendClassPath(subEnv.pathParts);
                if (!unseen.isEmpty()) {
                    return loadAfterCommandable(unseen, env, commands, outcomeClass, i + 1);
                }
            }
            if (!env.hiddenCommands.isEmpty()) {
                return execute(env, commands, outcomeClass, i + 1);
            }
        }
        
        if (outcomeClass != null) {
            for (Object object : env.parentOutputs.get(env.parentOutputs.size() -1)) {
                if (outcomeClass.equals(object.getClass())) {
                    return new Outcome(0, object);
                }
            }
        }
        
        return new Outcome(0, null);
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends Commandable> taskClass(Class taskClass) {
        return taskClass;
    }
    
    private Outcome load(Collection<PathPart> unseen, Environment env, final CommandNode CommandNode, final List<String> arguments, final int offset, final Class<?> outcomeClass) {
        final Executor childExecutor = new Executor(this);
        final Environment childEnv = new Environment(env, childExecutor);
        FutureTask<Outcome> future = new FutureTask<Outcome>(new Callable<Outcome>() {
            public Outcome call() {
                return childExecutor.resume(childEnv, CommandNode, arguments, offset, outcomeClass);
            }
        });
        return waitForOutcome(unseen, future);
    }

    private Outcome waitForOutcome(Collection<PathPart> parts, FutureTask<Outcome> future) {
        threadFactory.partsQueue.offer(parts);
        threadPool.execute(future);
        try {
            return Retry.retry(future);
        } catch (ExecutionException e) {
            throw new GoException(FUTURE_EXECUTION, e);
        }
    }

    Outcome start(InputOutput io, List<String> arguments, Class<?> outcomeClass) {
        Environment env = new Environment(library, io, this);
        readConfigurations(env.io);
        Artifact artifact = programs.get(flatten(arguments.get(0)));
        if (artifact != null) {
            Collection<PathPart> unseen = extendClassPath(new ResolutionPart(artifact));
            if (!unseen.isEmpty()) {
                return load(unseen, env, null, arguments, 0, outcomeClass);
            }
        }
        return resume(env, null, arguments, 0, outcomeClass);
    }

    public int run(InputOutput io, List<String> arguments) {
        return start(io, arguments, null).code;
    }

    public int run(InputOutput io, Object...arguments) {
        return run(io, flatten(arguments));
    }
    
    public <T> T run(Class<T> type, InputOutput io, Object...arguments) {
        return run(type, io, flatten(arguments));
    }
    
    public <T> T run(Class<T> type, InputOutput io, List<String> arguments) {
        Outcome outcome = start(io, arguments, type);
        if (outcome.object == null) {
            return null;
        }
        return type.cast(outcome.object);
    }

    private List<String> flatten(Object... arguments) {
        List<String> flattened = new ArrayList<String>();
        for (Object object : arguments) {
            if (object instanceof List<?>) {
                for (Object item : (List<?>) object) {
                    flattened.add(item.toString());
                }
            } else if (object.getClass().isArray()) {
                for (Object item : (Object[]) object) {
                    flattened.add(item.toString());
                }
            } else if (object instanceof Map<?, ?>) {
                for (Map.Entry<?, ?> e : ((Map<?, ?>) object).entrySet()) {
                    flattened.add("--" + e.getKey() + "=" + e.getValue());
                } 
            } else {
                flattened.add(object.toString());
            }
        }
        return flattened;
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
