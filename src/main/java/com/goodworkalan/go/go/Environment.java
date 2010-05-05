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
    
    /**
     * The set of path parts to add to the class path after the commandable
     * completes.
     */
    final Set<PathPart> pathParts = new LinkedHashSet<PathPart>();

    /**
     * The list of commandables to execute after the current commandable
     * completes.
     */
    final LinkedList<Commandable> hiddenCommands;
    
    /** The exit code assigned by the commandable. */
    Integer exitCode;

    /** The verbosity at each level of the stack. */
    final List<Integer> verbosity = new ArrayList<Integer>();

    /**
     * Create a new environment.
     * 
     * @param library
     *            The library.
     * @param io
     *            The input/output streams.
     * @param execution
     *            The execution state.
     */
    public Environment(Library library, InputOutput io, Executor executor) {
        this.commandableClass = null;
        this.library = library;
        this.io = io;
        this.executor = executor;
        this.hiddenCommands = new LinkedList<Commandable>();
    }

    /**
     * Internal structure creates a copy of the given environment with some
     * alterations. The given executor is used as the executor. The given
     * commandable class is used as an error message context. Command stack
     * based lists, lists that have an entry for each command in the command
     * stack, are copied up to the given stack limit
     * 
     * @param env
     *            The environment to copy.
     * @param executor
     *            The executor.
     * @param commandableClass
     *            The commandable class used as the error message context.
     * @param stackLimit
     *            The length of elements to copy from command stack based lists.
     */
    private Environment(Environment env, Executor executor, Class<? extends Commandable> commandableClass, int stackLimit) {
        this.commandableClass = commandableClass;
        this.library = env.library;
        this.io = env.io;
        this.executor = executor;
        this.commands.addAll(env.commands.subList(0, stackLimit));
        this.arguments.addAll(env.arguments.subList(0, stackLimit));
        this.conversions.addAll(env.conversions.subList(0, stackLimit));
        this.parentOutputs.addAll(env.parentOutputs);
        this.verbosity.addAll(env.verbosity);
        this.hiddenCommands = env.hiddenCommands;
        if (stackLimit == env.commands.size()) {
            this.remaining.addAll(env.remaining);
        }
    }

    /**
     * Copy the given environment using the given commandable class as the error
     * message context and copying the command stack based lists up to the given
     * stack limit.
     * 
     * @param env
     *            The environment to copy.
     * @param commandableClass
     *            The commandable class used as the error message context.
     * @param stackLimit
     *            The length of elements to copy from command stack based lists.
     */
    Environment(Environment env, Class<? extends Commandable> commandableClass, int stackLimit) {
        this(env, env.executor, commandableClass, stackLimit);
    }

    /**
     * Create a copy of the given environment with the given executor.
     * 
     * @param env
     *            The environment to copy.
     * @param executor
     *            The executor.
     */
    Environment(Environment env, Executor executor) {
        this(env, executor, env.commandableClass, env.commands.size());
    }

    /**
     * Format the message format with the given key using the given arguments
     * and write to standard error if the verbose argument has been specified at
     * least once on the command line for this command or any parent commands.
     * The message bundle message bundle found in the package of the commandable
     * class. The message format is found in a using a key made by catenating
     * the commandable class name, a slash, and the given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
     */
    public void verbose(String messageKey, Object...arguments) {
        verbose(commandableClass, messageKey, arguments);
    }

    /**
     * Format the message format in the message bundle found in the package of
     * the given context class with the given key using the given arguments and
     * write to standard error if the verbose argument has been specified at
     * least once on the command line for this command or any parent commands.
     * The message bundle message bundle found in the package of the given
     * context class. The message format is found in a using a key made by
     * catenating the context class name, a slash, and the given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
     */
    public void verbose(Class<?> context, String token, Object... arguments) {
        error(1, context, token, arguments);
    }

    /**
     * Format the message format with the given key using the given arguments
     * and write to standard error if the verbose argument has been specified at
     * least twice on the command line for this command or any parent commands.
     * The message bundle message bundle found in the package of the commandable
     * class. The message format is found in a using a key made by catenating
     * the commandable class name, a slash, and the given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
      */
    public void debug(String message, Object...arguments) {
        debug(commandableClass, message, arguments);
    }

    /**
     * Format the message format in the message bundle found in the package of
     * the given context class with the given key using the given arguments and
     * write to standard error if the verbose argument has been specified at
     * least twice on the command line for this command or any parent commands.
     * The message bundle message bundle found in the package of the given
     * context class. The message format is found in a using a key made by
     * catenating the context class name, a slash, and the given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
     */
    public void debug(Class<?> context, String token, Object...arguments) {
        error(2, context, token, arguments);
    }

    /**
     * Format the message format in the message bundle found in the package of
     * the given context class with the given key using the given arguments and
     * write to standard error if the verbose argument has been specified at
     * least as many times as the given level on the command line for this
     * command or any parent commands. The message bundle message bundle found
     * in the package of the given context class. The message format is found in
     * a using a key made by catenating the context class name, a slash, and the
     * given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
     */
    public void error(int level, Class<?> context, String token, Object...arguments) {
        for (int i = 0, stop = verbosity.size(); i < stop; i++) {
            if (verbosity.get(i) >= level) {
                error(context, token, arguments);
                break;
            }
        }
    }

    /**
     * Format the message format in the message bundle found in the package of
     * the given context class with the given key using the given arguments and
     * write to standard error. The message bundle message bundle found
     * in the package of the given context class. The message format is found in
     * a using a key made by catenating the context class name, a slash, and the
     * given message key.
     * 
     * @param messageKey
     *            The message format key.
     * @param arguments
     *            The message format arguments.
     */
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
        for (int i = 0, stop = arguments.length; i < stop; i++) {
            if (arguments[i] instanceof Class<?>) {
                arguments[i] = ((Class<?>) arguments[i]).getCanonicalName();
            }
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
     * <p>
     * This method means that a command can output only one instance of a
     * particular class, since only the first output of a particular class will
     * ever be returned. If two objects of the same class must be returned,
     * enclose them in another class.
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

    /**
     * Extend the current class path with the given part for all descendant
     * commands in the command line. If any of the path parts are not in the
     * current class path, a new class loader is created that will lookup
     * classes in the missing path parts and a new thread is launched to execute
     * the descendant commands in the command line. The extended class path will
     * apply to any commands added by the {@link #invokeAfter(Commandable)
     * invokeAfter} method.
     * 
     * @param pathPart
     *            The path part to add to the class path.
     */
    public void extendClassPath(PathPart pathPart) {
        pathParts.add(pathPart);
    }

    /**
     * Execute the given commandable after this commandable and before any
     * commandables mapped to descendant commands in the command line. Multple
     * commandables added with this method will be executed in the order in
     * which they are added. Commandables added by this method can in turn add
     * commandables using this method on their environments.
     * 
     * @param commandable
     *            The commandable to insert into the command hierarchy.
     */
    public void invokeAfter(Commandable commandable) {
        hiddenCommands.add(commandable);
    }

    /**
     * Record the given output object. The output object is available to other
     * commands as a return object requested by class, or through one of the get
     * methods locatable by class and heirarchy index or class and command line
     * arguments FIXME (hmm...shouldn't that be run?).
     * 
     * @param output
     *            The output.
     */
    public void output(Object output) {
        outputs.add(output);
    }

    /**
     * Add a command to the list of command names creating a new element in each
     * of the command stack based lists.
     * 
     * @param command
     *            The name of the command to add.
     */
    void addCommand(String command) {
        commands.add(command);
        arguments.add(new ArrayList<String>());
        conversions.add(new ArrayList<Conversion>());
        verbosity.add(0);
    }

    /**
     * Get the meta information for the giveen commandable class.
     * 
     * @param commandableClass
     *            The commandable class.
     * @return The meta information for the given commandable class or null if
     *         the given commandable class has not been loaded.
     */
    public MetaCommand getMetaCommand(Class<? extends Commandable> commandableClass) {
        return executor.commandNodes.get(commandableClass);
    }

    /**
     * Set the exit code for the execution. The exit code will be set after the
     * {@link Commandable#execute(Environment) execute} method of the
     * commandable completes and the current command will terminate.
     * <p>
     * No further commands will be executed in the command line. The error code
     * will be returned to the caller, or if the caller is expecting an output
     * object and the exit code is non-zero, an unchecked exception will be
     * raised for the caller to catch.
     * <p>
     * To exit before the commandable completes, raise an {@link Exit}
     * exception. The result for the caller will be the same as calling this
     * method.
     * <p>
     * System exit is called only if this is the first command run and the the
     * caller who initiated the entire program uses the result of execution as
     * the system exit code. Calling the various <code>run</code> methods of the
     * <code>Executor</code> will not cause the system to exit. They will
     * instead return the exit code of the sub command or raise an exception if
     * the caller is expecting an output object as a return value. The
     * {@link Go#execute(List, String...) Go.execute} method returns the exit
     * code of the first command executed. Whether or not that is used as a
     * parameter to system exit is determined by the actions of the
     * <code>Go.execute</code> caller.The Jav-a-Go-Go boot class invoked with
     * the commnad <code>java go.go</code> does, in fact, use the result of
     * <code>Go.execute</code> as the system exit.
     * 
     * @param code
     *            The exit code.
     */
    public void exit(int code) {
        this.exitCode = code;
    }
}
