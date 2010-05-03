package com.goodworkalan.go.go;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.goodworkalan.infuse.StringConstructorInfuser;
import com.goodworkalan.infuse.StringInfuser;
import com.goodworkalan.reflective.Field;
import com.goodworkalan.reflective.Method;
import com.goodworkalan.utility.Primitives;

/**
 * A wrapper around a task that maps task properties to assignment and parameter
 * conversion strategies for those properties, and collects the sub commands of
 * the task.
 * 
 * @author Alan Gutierrez
 */
class Responder implements MetaCommand {
    /** The task class. */
    private final Class<? extends Commandable> taskClass;

    /** The name of the command. */
    private final String name;

    /** The parent task if any. */
    private final Class<? extends Commandable> parent;

    /** A map of verbose argument names to assignment strategies. */
    private final Map<String, Assignment> assignments;

    /** A map of sub command names to sub command responders. */ 
    public final Map<String, Responder> commands;
    
    private final Map<String, Class<?>> arguments;

    /**
     * Create wrapper around the given class that implements
     * <code>Commandable</code>.
     * 
     * @param taskClass
     *            The task class.
     */
    public Responder(Class<? extends Commandable> taskClass) {
        this.assignments = new TreeMap<String, Assignment>();
        
        Command command = taskClass.getAnnotation(Command.class);
        String name;
        if (command == null || command.name().equals("")) {
            String className = taskClass.getCanonicalName();
            className = className.replaceFirst("^.*\\.", "");
            className = className.replaceFirst("Command$", "");
            name = hyphenate(className);
        } else {
            name = command.name();
        }
        Class<? extends Commandable> parent = null;
        if (command != null && !command.parent().equals(Commandable.class)) {
            parent = command.parent();
        }

        gatherAssignments(taskClass, assignments);

        Map<String, Class<?>> arguments = new HashMap<String, Class<?>>();
        for (Map.Entry<String, Assignment> entry : assignments.entrySet()) {
            arguments.put(entry.getKey(), entry.getValue().getType());
        }

        this.arguments = Collections.unmodifiableMap(arguments);
        this.taskClass = taskClass;
        this.name = name;
        this.parent = parent;
        this.commands = new TreeMap<String, Responder>();
    }

    /**
     * Add a sub command responder. The sub command will be made available
     * through the {@link #getCommand(String) getCommand} method indexed by the
     * command name property of the responder.
     * 
     * @param responder
     *            The sub command to add.
     */
    public void addCommand(Responder responder) {
        commands.put(responder.getName(), responder);
    }

    /**
     * Get the command line name of the command.
     * 
     * @return The command line name of the command.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the task class.
     * 
     * @return The task class.
     */
    public Class<? extends Commandable> getCommandClass() {
        return taskClass;
    }

    /**
     * Get the parent task.
     * 
     * @return The parent task for null if none.
     */
    public Class<? extends Commandable> getParentCommandClass() {
        return parent;
    }

    public Map<String, Assignment> getAssignments() {
        return assignments;
    }
    
    public Map<String, Class<?>> getArguments() {
        return arguments;
    }
    
    /**
     * Convert a camel case name into a command line hyphenated name by
     * replacing capital letters with lower case letters preceeded by hyphens.
     * 
     * @param camelCase
     *            The camel case name.
     * @return A hyphenated name.
     */
    public static String hyphenate(String camelCase) {
        StringBuilder string = new StringBuilder();
        string.append(Character.toLowerCase(camelCase.charAt(0)));
        for (int i = 1, stop = camelCase.length(); i < stop; i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                string.append('-').append(Character.toLowerCase(ch));
            } else {
                string.append(ch);
            }
        }
        return string.toString();
    }

    public static void gatherAssignments(Class<?> arguableClass, Map<String, Assignment> assignment) {
        for (java.lang.reflect.Method method : arguableClass.getMethods()) {
            if (method.getName().startsWith("add")
                && method.getName().length() != 3
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterTypes().length == 1) {
                Argument argument = method.getAnnotation(Argument.class);
                if (argument != null) {
                    String verbose = argument.value();
                    if (verbose.equals("")) {
                        String name = method.getName();
                        name = name.substring(3);
                        name = name.substring(0, 1).toLowerCase() + name.substring(1);
                        verbose = hyphenate(name);
                    }
                    if (assignment.containsKey(verbose)) {
                        throw new GoException(0);
                    }
                    Class<?> type =  Primitives.box(method.getParameterTypes()[0]);
                    if (type.equals(String.class)) {
                        assignment.put(verbose, new Assignment(new MethodSetter(new Method(method)), StringInfuser.INSTNACE));
                    } else {
                        assignment.put(verbose, new Assignment(new MethodSetter(new Method(method)), new StringConstructorInfuser(type)));
                    }
                }
            }
        }
        for (java.lang.reflect.Field field : arguableClass.getFields()) {
            Argument argument = field.getAnnotation(Argument.class);
            if (argument != null) {
                String verbose = argument.value();
                if (verbose.equals("")) {
                    verbose = hyphenate(field.getName());
                }
                if (assignment.containsKey(verbose)) {
                    throw new GoException(0);
                }
                Class<?> type =  Primitives.box(field.getType());
                if (type.equals(String.class)) {
                    assignment.put(verbose, new Assignment(new FieldSetter(new Field(field)), StringInfuser.INSTNACE));
                } else {
                    assignment.put(verbose, new Assignment(new FieldSetter(new Field(field)), new StringConstructorInfuser(type)));
                }
            }
        }
        Class<?> superclass = arguableClass.getSuperclass();
        if (Commandable.class.isAssignableFrom(superclass)) {
            gatherAssignments(superclass, assignment);
        }
    }
}
