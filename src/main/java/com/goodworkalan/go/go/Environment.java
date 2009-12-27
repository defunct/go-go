package com.goodworkalan.go.go;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The environment in which a task is executed.
 * 
 * @author Alan Gutierrez
 */
public class Environment {
    /** The input/output streams. */
    public final InputOutput io;

    /** The current execution. */
    public final Executor executor;

    /** The command part for the current task. */
    public final CommandPart part;
    
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
    public Environment(InputOutput io, CommandPart part, Executor executor) {
        this.io = io;
        this.part = part;
        this.executor = executor;
    }
    
    public void error(String message, Object...arguments) {
        error(0, part.getTaskClass(), message, arguments);
    }
    
    public void error(int indent, String message, Object...arguments) {
        error(indent, part.getTaskClass(), message, arguments);
    }
    public void error(Class<?> context, String token, Object...arguments) {
        error(0, context, token, arguments);
    }

    public void error(int indent, Class<?> context, String token, Object...arguments) {
        String className = context.getCanonicalName();
        int index = className.lastIndexOf('.');
        if (index > -1) {
            className = className.substring(index + 1);
        }
        String key = className + "/" + token; 
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(context.getPackage().getName() + ".stderr", Locale.getDefault(), Thread.currentThread().getContextClassLoader());
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(Environment.class.getPackage().getName() + ".stderr");
            key = "Environment/bundle.missing";
        }
        while (indent-- != 0) {
            io.err.print("    ");
        }
        try {
            io.err.println(String.format(bundle.getString(key), arguments));
        } catch (MissingResourceException e) {
            error(0, Environment.class, "message.missing");
        }
    }
}
