package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.go.go.library.PathPart;

/**
 * The environment in which a task is executed.
 * 
 * @author Alan Gutierrez
 */
public class Environment {
    final Class<? extends Commandable> commandableClass;
    
    /** The input/output streams. */
    public final InputOutput io;

    /** The current execution. */
    public final Executor executor;

    /** The library. */
    public final Library library;

    /** The list of commands. */
    public final LinkedList<String> commands = new LinkedList<String>();
    
    /** The list of generated objects. */
    final List<Object> outputs = new ArrayList<Object>();
    
    final List<List<Object>> parentOutputs = new ArrayList<List<Object>>();

    /** The list of converted arguments for the commands. */
    final LinkedList<List<Conversion>> conversions = new LinkedList<List<Conversion>>();
    
    /** The list of arguments for the commands. */
    public final LinkedList<List<String>> arguments = new LinkedList<List<String>>();
    
    /** The list of arguments remaining. */
    public final List<String> remaining = new ArrayList<String>();
    
    final Set<PathPart> pathParts = new LinkedHashSet<PathPart>();

    final LinkedList<Commandable> hiddenCommands;

    /** The verbosity. */
    public final int verbosity;
    
    /**
     * Create a new environment.
     * 
     * @param io
     *            The input/output streams.
     * @param part
     *            The command part for the current task.
     * @param execution
     *            The execution state.
     */
    public Environment(Library library, InputOutput io, Executor executor) {
        this.commandableClass = null;
        this.library = library;
        this.io = io;
        this.executor = executor;
        this.verbosity = 0;
        this.hiddenCommands = new LinkedList<Commandable>();
    }

    /**
     * Necessary to keep vebosity final, the only way to increment verbosity is
     * to create a new object.
     * 
     * @param env
     *            The environment to copy.
     * @param verbosityIncrement
     *            The value to add to the verbosity.
     */
    private Environment(Environment env, Class<? extends Commandable> commandableClass, int offset, int verbosityIncrement, Executor executor) {
        this.commandableClass = commandableClass;
        this.library = env.library;
        this.io = env.io;
        this.executor = executor;
        this.commands.addAll(env.commands.subList(0, offset));
        this.arguments.addAll(env.arguments.subList(0, offset));
        this.conversions.addAll(env.conversions.subList(0, offset));
        this.parentOutputs.addAll(env.parentOutputs);
        this.verbosity = env.verbosity + verbosityIncrement;
        this.hiddenCommands = env.hiddenCommands;
        if (offset == env.commands.size()) {
            this.remaining.addAll(env.remaining);
        }
    }
    
    Environment(Environment env, int verbosityIncrement) {
        this(env, env.commandableClass, env.commands.size(), verbosityIncrement, env.executor);
    }
    
    Environment(Environment env, Class<? extends Commandable> commandableClass, int offset) {
        this(env, commandableClass, offset, 0, env.executor);
    }
    
    Environment(Environment env, Executor executor) {
        this(env, env.commandableClass, env.commands.size(), 0, executor);
    }
    
    public void verbose(String message, Object...arguments) {
        verbose(commandableClass, message, arguments);
    }

    public void verbose(Class<?> context, String token, Object...arguments) {
        error(1, context, token, arguments);
    }
    
    public void debug(String message, Object...arguments) {
        debug(commandableClass, message, arguments);
    }

    public void debug(Class<?> context, String token, Object...arguments) {
        error(2, context, token, arguments);
    }

    public void error(int level, Class<?> context, String token, Object...arguments) {
        if (verbosity >= level) {
            error(context, token, arguments);
        }
    }

    public void error(Class<?> context, String token, Object...arguments) {
        error(io, context, token, arguments);
    }

    /**
     * Display a formatted error message on the error stream in the given I/O
     * bundle. The message is formatted using a message bundle named
     * "stderr.properties" that is found in the same package as the given
     * context class. The bundle string key is the class name joined with the
     * given message token separated by a slash as in "TouchCommand/diskFull".
     * <p>
     * The bundle format strings are printf style <code>String.format</code>
     * format strings. The given arguments are passed to the format.
     * 
     * @param io
     *            The I/O bundle.
     * @param context
     *            The context used to find the bundle and half of the message
     *            key.
     * @param token
     *            The message token.
     * @param arguments
     *            Arguments to pass to the format.
     */
    public static void error(InputOutput io, Class<?> context, String token, Object...arguments) {
        String className = context.getCanonicalName();
        className = className.substring(className.lastIndexOf('.') + 1);
        String key = className + "/" + token; 
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(context.getPackage().getName() + ".stderr", Locale.getDefault(), Thread.currentThread().getContextClassLoader());
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(Environment.class.getPackage().getName() + ".stderr");
            key = "Environment/bundle.missing";
        }
        try {
            io.err.println(String.format(bundle.getString(key), arguments));
        } catch (MissingResourceException e) {
            error(io, Environment.class, "message.missing", key);
        } catch (MissingFormatArgumentException e) {
            error(io, Environment.class, "argument.missing", e.getMessage());
        }
    }

    /**
     * Get the output of the given type from the list of outputs generated by
     * the parent command at the given index.
     * 
     * @param <T>
     *            The type of output.
     * @param type
     *            The type of output.
     * @param index
     *            The index of the parent command.
     * @return The output object or null if none exists.
     */
    public <T> T get(Class<T> type, int index) {
        for (Object object : parentOutputs.get(index)) {
            if (object.getClass().equals(type)) {
                return type.cast(object);
            }
        }
        return null;
    }
    
    /**
     * Get the output of the given type from the list of outputs generated by
     * the parent command at the given index.
     * 
     * @param <T>
     *            The type of output.
     * @param type
     *            The type of output.
     * @param index
     *            The index of the parent command.
     * @return The output object or null if none exists.
     */
    public <T> T get(Class<T> type, List<List<String>> command) {
        for (Object object : executor.getCache(command)) {
            if (object.getClass().equals(type)) {
                return type.cast(object);
            }
        }
        return null;
    }

    public void extendClassPath(PathPart part) {
        pathParts.add(part);
    }

    public void invokeAfter(Commandable commandable) {
        hiddenCommands.add(commandable);
    }
    
    public void output(Object output) {
        outputs.add(output);
    }
    
    void addCommand(String command) {
        commands.add(command);
        arguments.add(new ArrayList<String>());
        conversions.add(new ArrayList<Conversion>());
    }
    
    public MetaCommand getMetaCommand(Class<? extends Commandable> commandClass) {
        return executor.responders.get(commandClass);
    }
}
