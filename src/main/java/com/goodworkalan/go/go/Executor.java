package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.ASSIGNMENT_EXCEPTION_THROWN;
import static com.goodworkalan.go.go.GoException.ASSIGNMENT_FAILED;
import static com.goodworkalan.go.go.GoException.CANNOT_CREATE_TASK;
import static com.goodworkalan.go.go.GoException.COMMAND_CLASS_MISSING;

import java.io.BufferedReader;
import java.io.IOException;
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
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.PathParts;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.infuse.InfusionException;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;
import com.goodworkalan.retry.Retry;
import com.goodworkalan.utility.Primitives;

public class Executor {
    /** Used to construct commands. */
    private final ReflectiveFactory reflective;

    /** A map of commands and their arguments to a list of their outputs. */ 
    private final Map<List<List<String>>, List<Object>> cache = new HashMap<List<List<String>>, List<Object>>();

    /** The parent executor in the thread that spawned this executor or null. */
    private final Executor parent;

    private final Map<List<String>, Artifact> programs;

    /** A set of keys of artifacts that have already been included. */
    private final Set<List<String>> seen = new HashSet<List<String>>();
    
    /** The set of class path URLs that have been inspected for commands. */
    private final Set<URL> urls = new HashSet<URL>();

    /** The Java-a-Go-Go library. */
    private final Library library;
    
    /** The root map of command names to command responders. */
    private final Map<String, Responder> commands = new TreeMap<String, Responder>();

    /** The map of tasks to responders. */
    final Map<Class<? extends Commandable>, Responder> responders = new HashMap<Class<? extends Commandable>, Responder>();

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
    public Executor(ReflectiveFactory reflective, Library library, Map<List<String>, Artifact> programs) {
        seen.add(Include.exclude("com.goodworkalan/danger"));
        seen.add(Include.exclude("com.goodworkalan/verbiage"));
        seen.add(Include.exclude("com.goodworkalan/go-go"));
        seen.add(Include.exclude("com.goodworkalan/infuse"));
        seen.add(Include.exclude("com.goodworkalan/class-boxer"));
        seen.add(Include.exclude("com.goodworkalan/class-association"));
        seen.add(Include.exclude("com.goodworkalan/reflective"));
        this.programs = programs;
        this.reflective = reflective;
        this.library = library;
        this.parent = null;
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
        this.responders.putAll(parent.responders);
        this.parts.addAll(parent.parts);
    }

    /**
     * Iterate through all of the command interpreter task and dependencies
     * definition files that can be found using the given class loader if they
     * definition files have not already been loaded. Any dependency classes
     * that specify new artifacts will have those artifacts loaded with the
     * given artifacts reader.
     * 
     * @param reader
     *            The artifacts reader.
     * @param classLoader
     *            The parent class loader.
     * @return A class loader the includes any newly specified artifacts or null
     *         if no new artifacts were specified.
     * @throws IOException
     *             For any I/O error while reading the command interpreter
     *             definition files.
     */
    Collection<PathPart> addArtifacts(Artifact...artifacts) {
        List<Artifact> unseen = new ArrayList<Artifact>();
        for (Artifact artifact : artifacts) {
            List<String> key = artifact.getKey().subList(0, 2);
            if (!seen.contains(key)) {
                unseen.add(artifact);
            }
        }
        Collection<PathPart> subPath = new ArrayList<PathPart>();
        if (!unseen.isEmpty()) {
            for (Artifact artifact : unseen) {
                subPath.add(new ResolutionPart(artifact));
            }
            subPath = library.resolve(subPath, seen);
            for (PathPart part : subPath) {
                seen.add(part.getArtifact().getUnversionedKey());
            }
        }
        return subPath;
    }

    private void readConfigurations() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources("META-INF/services/com.goodworkalan.go.go.Commandable");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!urls.contains(url)) {
                    Set<Class<? extends Commandable>> tasks = new HashSet<Class<? extends Commandable>>();
                    
                    urls.add(url);
                    BufferedReader lines = new BufferedReader(new InputStreamReader(url.openStream()));
                    String className;
                    try {
                        while ((className = lines.readLine()) != null) {
                            if (className.trim().equals("")) {
                                continue;
                            }
                            Class<?> foundClass;
                            try {
                                foundClass = classLoader.loadClass(className);
                            } catch (ClassNotFoundException e) {
                                throw new GoException(0, e);
                            }
                            if (Commandable.class.isAssignableFrom(foundClass)) {
                                tasks.add(taskClass(foundClass));
                            } else {
                                throw new GoException(0);
                            }
                        }
                    } catch (IOException e) {
                        throw new GoException(0, e);
                    }
                    for (Class<? extends Commandable> taskClass : tasks) {
                        if (!responders.containsKey(taskClass)) {
                            Responder responder = new Responder(taskClass);
                            responders.put(taskClass, responder);
                            Class<? extends Commandable> parentTaskClass = null;
                            while ((parentTaskClass = responder.getParentTaskClass()) != null) {
                                Responder parent = responders.get(parentTaskClass);
                                if (parent == null) {
                                    parent = new Responder(parentTaskClass);
                                    responders.put(parentTaskClass, parent);
                                }
                                parent.addCommand(responder);
                                responder = parent;
                            }
                            // Will reassign sometimes, but that's okay.
                            commands.put(responder.getName(), responder);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
    }

    private Outcome resume(Environment env, Responder responder, List<String> arguments, int offset, Class<?> outcomeClass) {
        if (offset == 0) {
            responder = commands.get(arguments.get(0));
            if (responder == null) {
                throw new GoException(COMMAND_CLASS_MISSING, arguments.get(offset));
            }
            env.addCommand(arguments.get(0));
            offset++;
        }
        return extend(env, responder, arguments, offset, outcomeClass);
    }

    private Environment argument(Environment env, Responder responder, String name, String value) {
        String command = env.commands.getLast();
        if (name.indexOf(':') == -1) {
            name = command + ':' + name;
        }
        String[] qualified = name.split(":");
        if (qualified.length != 2) {
            throw new GoException(0);
        }

        Class<? extends Commandable> parent = responder.getParentTaskClass();
        Responder actualResponder; 
        if (parent == null) {
            actualResponder = commands.get(qualified[0]);
        } else {
            actualResponder = responders.get(parent).commands.get(qualified[0]);
        }
        
        if (actualResponder == null) {
            throw new GoException(0);
        }

        Assignment assignment = null;
        
        if(qualified[1].equals("verbose")) {
            return new Environment(env, 1);
        }

        if (qualified[1].equals("no-verbose")) {
            return new Environment(env, -1);
        }

        // Check for a negated boolean flag.
        if (value == null) {
            // FIXME Ensure that there are no arguments beginning with no.
            if (qualified[1].startsWith("no-")) {
                String negate = qualified[1].substring(3);
                    assignment = actualResponder.getAssignments().get(negate); 
                    if (assignment != null) {
                        if (Primitives.box(assignment.getType()).equals(Boolean.class)) {
                            name = actualResponder.getName() + ':' + negate;
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
            assignment = actualResponder.getAssignments().get(qualified[1]); 
            if (assignment == null) {
                throw new GoException(0);
            }
            if (value == null && Primitives.box(assignment.getType()).equals(Boolean.class)) {
                value = "true";
            }
        }
        
        try {
            env.conversions.getLast().add(new Conversion(qualified[0], qualified[1], assignment.infuser.infuse(value)));
        } catch (InfusionException e) {
            throw new GoException(0, e);
        }
        env.arguments.getLast().add("--" + qualified[0] + ':' + qualified[1] + '=' + value);

        return env;
    }
    
    private Outcome extend(Environment env, Responder responder, List<String> arguments, int offset, Class<?> outcomeClass) {
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
                    env = argument(env, responder, name, value);
                } else {
                    Artifact artifact = programs.get(flatten(env.commands, argument));
                    if (artifact != null) {
                        Collection<PathPart> unseen = addArtifacts(artifact);
                        if (!unseen.isEmpty()) {
                            return load(unseen, env, responder, arguments, i, outcomeClass);
                        }
                    }
                    responder = responder.commands.get(argument);
                    if (responder == null) {
                        env.remaining.add(argument);
                        remaining = true;
                    } else {
                        env.addCommand(argument);
                    }
                }
            }
        }
        return execute(env, outcomeClass);
    }

    private Outcome execute(Environment env, Class<?> outcomeClass) {
        Map<String, Responder> byCommand = commands; 
        // Go through each command executing if it is not cached.
        for (int i = 0, stop = env.commands.size(); i < stop; i++) {
            // Find the responder.
            Responder responder = byCommand.get(env.commands.get(i));
            
            // Set up the lookup of a child responder.
            byCommand = responder.commands;
            
            // Create an instance of the commandable.
            Commandable commandable;
            try {
                commandable = reflective.newInstance(responder.getTaskClass());
            } catch (ReflectiveException e) {
                throw new GoException(CANNOT_CREATE_TASK, e, responder.getTaskClass().getCanonicalName());
            }
            
            // Set the arguments for the command.
            for (Conversion conversion : env.conversions.get(i)) {
                if (conversion.command.equals(responder.getName())) {
                    Assignment assignment = responder.getAssignments().get(conversion.getName());
                    try {
                        assignment.setter.set(commandable, conversion.getValue());
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
            commandable.execute(subEnv);
            
            if (!subEnv.pathParts.isEmpty()) {
                
            }
            
            env.parentOutputs.add(subEnv.outputs);
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
    
    Outcome load(Collection<PathPart> unseen, final Environment env, final Responder responder, final List<String> arguments, final int offset, final Class<?> outcomeClass) {
        final Executor executor = new Executor(this);
        FutureTask<Outcome> future = new FutureTask<Outcome>(new Callable<Outcome>() {
            public Outcome call() {
                return executor.resume(env, responder, arguments, offset, outcomeClass);
            }
        });
        Thread thread = new Thread(future);
        thread.setContextClassLoader(PathParts.getClassLoader(unseen, Thread.currentThread().getContextClassLoader()));
        thread.run();
        try {
            return Retry.retry(future);
        } catch (ExecutionException e) {
            if (e instanceof Erroneous) {
                return new Outcome(((Erroneous) e).getCode(), null);
            }
            return new Outcome(1, null);
        }
    }

    Outcome start(InputOutput io, List<String> arguments, Class<?> outcomeClass) {
        readConfigurations();
        Environment env = new Environment(library, io, this);
        Artifact artifact = programs.get(flatten(arguments.get(0)));
        if (artifact != null) {
            Collection<PathPart> unseen = addArtifacts(artifact);
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
}
