package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.ilk.Ilk;

/**
 * The environment in which a task is executed.
 * 
 * @author Alan Gutierrez
 */
public class Environment {
    /** The currently executing commandable class. */
    private final Class<? extends Commandable> commandableClass;
    
    /** The index of the command. */
    private final int index;
    
    /** The input/output streams. */
    public final InputOutput io;

    /** The current execution. */
    public final Executor executor;

    /** The library. */
    public final Library library;

    /** The list of commands. */
    public final LinkedList<String> commands = new LinkedList<String>();
    
    /** The list of maps of generated objects by type. */
    final List<List<Ilk.Box>> outputs = new ArrayList<List<Ilk.Box>>();
    
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
    final LinkedList<Class<? extends Commandable>> commandables;

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
        this.index = 0;
        this.executor = executor;
        this.commandables = new LinkedList<Class<? extends Commandable>>();
    }

    /**
     * Creates a copy of the given environment but with a new executor, a new commandable class
     * for the error message context and a new command index.
     * 
     * @param env
     *            The environment to copy.
     * @param executor
     *            The executor.
     * @param commandableClass
     *            The commandable class used as the error message context.
     * @param commandIndex
     *            The command index.
     */
    private Environment(Environment env, Executor executor, Class<? extends Commandable> commandableClass, int commandIndex) {
        this.commandableClass = commandableClass;
        this.library = env.library;
        this.io = env.io;
        this.index = commandIndex;
        this.executor = executor;
        this.commands.addAll(env.commands);
        this.arguments.addAll(env.arguments);
        this.conversions.addAll(env.conversions);
        this.verbosity.addAll(env.verbosity);
        this.outputs.addAll(env.outputs);
        this.commandables = env.commandables;
        this.remaining.addAll(env.remaining);
    }

    /**
     * Creates a copy of the given environment but with a new commandable class
     * for the error message context and a new command index.
     * 
     * @param env
     *            The environment to copy.
     * @param executor
     *            The executor.
     * @param commandableClass
     *            The commandable class used as the error message context.
     * @param commandIndex
     *            The command index.
     */
    Environment(Environment env,  Class<? extends Commandable> commandableClass, int commandIndex) {
        this(env, env.executor, commandableClass, commandIndex);
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
        this(env, executor, env.commandableClass, env.index);
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
     * particular class, since only the last output of a particular class will
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
        return get(new Ilk<T>(type), index);
    }

    /**
     * Get the output of the type given by the given super type token from the
     * list of outputs generated by the parent command at the given index. The
     * method will return the most derived type with the most derived type
     * parameters that is assignable to the given type.
     * <p>
     * This method means that a command can output only one instance of a
     * particular class, since only the last output of a particular class will
     * ever be returned. If two objects of the same type must be returned,
     * enclose them in another class.
     * 
     * @param <T>
     *            The type of output.
     * @param ilk
     *            The super type token.
     * @param index
     *            The index of the parent command.
     * @return The output object or null if none exists.
     */
    public <T> T get(Ilk<T> ilk, int index) {
        Ilk.Box box = get(ilk.key, index);
        if (box == null) {
            return null;
        }
        return box.cast(ilk);
    }
    
    Ilk.Box get(Ilk.Key key , int index) {
        Ilk.Box candidate = null;
        for (Ilk.Box box : outputs.get(index)) {
            if (key.isAssignableFrom(box.key)
            && (candidate == null || candidate.key.isAssignableFrom(box.key))) {
                candidate = box;
            }
        }
        return candidate;
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
    public void invokeAfter(Class<? extends Commandable> commandable) {
        commandables.add(commandable);
    }

    /**
     * Record the given output object and make it available to other commands as
     * a return object or through the {@link #get(Class, int) get} method. Only
     * one output object per type is allowed, so the last assignment of an
     * object
     * 
     * @param outputClass
     *            The output class.
     * @param output
     *            The output.
     */
    public <T> void output(Class<T> outputClass, T output) {
        Ilk.Box box = new Ilk<T>(outputClass).box(output);
        outputs.get(index).add(box);
    }

    /**
     * Record the given output object with the type given by the given super
     * type token and make it available to other commands as a return object or
     * through the {@link #get(Class, int) get} method.
     * 
     * @param ilk
     *            The super type token.
     * @param output
     *            The output.
     */
    public <T> void output(Ilk<T> ilk, T output) {
        Ilk.Box box = ilk.box(output);
        outputs.get(index).add(box);
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
     * Set the exit code for the execution and raise an exception terminiating
     * the execution of the current {@link Commandable} immediately.
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
        throw new Exit(code);
    }
    
    public List<String> getCommandKey() {
        return getCommandKey(0, commands.size());
    }
    public List<String> getCommandKey(int fromIndex, int toIndex) {
        List<List<String>> components = new ArrayList<List<String>>();
        for (int i = 0, stop = commands.size(); i < stop; i++) {
            List<String> key = new ArrayList<String>();
            key.add(commands.get(i));
            key.addAll(arguments.get(i));
            components.add(key);
        }
        List<String> key = new ArrayList<String>();
        for (List<String> component : components.subList(fromIndex, toIndex)) {
            key.addAll(component);
        }
        if (toIndex == commands.size()) {
            key.addAll(remaining);
        }
        return key;
    }
    
    public List<String> getCommandLine() {
        return getCommandLine(0, commands.size());
    }

    public List<String> getCommandLine(int fromIndex) {
        return getCommandLine(fromIndex, commands.size());
    }
    
    public List<String> getCommandLine(int fromIndex, int toIndex) {
        if (toIndex > commands.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        List<String> commandLine = new ArrayList<String>();
        for (int i = fromIndex; i < toIndex; i++) {
            commandLine.add(commands.get(i));
            for (String argument : arguments.get(i)) {
                commandLine.add(argument);
            }
        }
        return commandLine;
    }
    

    public static List<String> flatten(Object... arguments) {
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
}
