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
 * A wrapper around a commandable that collects the sub commands of the
 * commandable and maps commandable properties to assignment and parameter
 * conversion strategies for those properties.
 * <p>
 * Not the most exciting name for a class. Basically, I ran out of names. A
 * command is a command. I needed to use Command for the annotation so it would
 * look nice above a class with command in its name. It was a hard choice to use
 * it there, because I wanted to use it for the public interface MetaCommand,
 * but the command is the command (unless I wanted to say the whole shebang is a
 * command then the user implementations become command implementations, but I
 * prefer to give the user the best names.) {@link Commandable} was a clever way
 * to to describe the behavior so I used that for the interface. I've taken to
 * prefixing collections of reflected data Meta. This would be MetaCommand but I
 * needed to expose that to the user. This class was called
 * <code>Repsonder</code> for a while. Not sure where I got that from. I'm
 * calling this a CommandNode since it is in fact, a participant in a tree
 * representing the command structure. If it where not part of a tree, I'd
 * probably have to call it CommandElement, which will become my standard suffix
 * for the internal bundle of meta data associated with a user facing class or
 * interface.
 * 
 * @author Alan Gutierrez
 */
class CommandNode implements MetaCommand {
    /** The task class. */
    private final Class<? extends Commandable> taskClass;

    /** The name of the command. */
    private final String name;

    /** The parent task if any. */
    private final Class<? extends Commandable> parent;

    /** The map of verbose argument names to assignment meta information. */
    private final Map<String, Assignment> assignments;

    /** The map of sub command names to sub command responders. */ 
    public final Map<String, CommandNode> commands;
    
    /** The map of arguments accepted by the command to their types. */
    private final Map<String, Class<?>> arguments;

    /**
     * Create a meta inforamtion node in the hierarchy of available commands
     * that contains the given commandable class.
     * 
     * @param commandableClass
     *            The task class.
     */
    public CommandNode(Class<? extends Commandable> commandableClass) {
        this.assignments = new TreeMap<String, Assignment>();
        
        Command command = commandableClass.getAnnotation(Command.class);
        String name;
        if (command == null || command.name().equals("")) {
            String className = commandableClass.getCanonicalName();
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

        gatherAssignments(commandableClass, assignments);

        Map<String, Class<?>> arguments = new HashMap<String, Class<?>>();
        for (Map.Entry<String, Assignment> entry : assignments.entrySet()) {
            arguments.put(entry.getKey(), entry.getValue().setter.getType());
        }

        this.arguments = Collections.unmodifiableMap(arguments);
        this.taskClass = commandableClass;
        this.name = name;
        this.parent = parent;
        this.commands = new TreeMap<String, CommandNode>();
    }

    /**
     * Add a sub command responder. The sub command will be made available
     * through the {@link #getCommand(String) getCommand} method indexed by the
     * command name property of the responder.
     * 
     * @param responder
     *            The sub command to add.
     */
    public void addCommand(CommandNode responder) {
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

    /**
     * Get the map of argument names to their assignment meta information.
     * 
     * @return The assignments.
     */
    public Map<String, Assignment> getAssignments() {
        return assignments;
    }

    /**
     * Get the map of arguments accepted by the command to their types.
     * 
     * @return The map of arguments accepted by the command to their types.
     */
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

    private static void gatherAssignments(Class<?> arguableClass, Map<String, Assignment> assignment) {
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
