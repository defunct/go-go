package com.goodworkalan.go.go;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.goodworkalan.infuse.Infuser;
import com.goodworkalan.reflective.setter.FieldSetter;
import com.goodworkalan.reflective.setter.MethodSetter;
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
 * <code>Responder</code> for a while. Not sure where I got that from. I'm
 * calling this a CommandNode since it is in fact, a participant in a tree
 * representing the command structure. If it where not part of a tree, I'd
 * probably have to call it CommandElement, which will become my standard suffix
 * for the internal bundle of meta data associated with a user facing class or
 * interface.
 * 
 * @author Alan Gutierrez
 */
class CommandNode implements MetaCommand {
    /** The <code>Infuser</code> used to convert from String to scalar object. */
    private final static Infuser INFUSER = new Infuser();
    
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
     * Create a meta information node in the hierarchy of available commands
     * that contains the given commandable class.
     * 
     * @param commandableClass
     *            The task class.
     */
    public CommandNode(InputOutput io, Class<? extends Commandable> commandableClass) {
        this.assignments = new TreeMap<String, Assignment>();
        
        String name = null;
        Command command = commandableClass.getAnnotation(Command.class);
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

        gatherAssignments(io, commandableClass, assignments);

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
     * Add a sub command command node.
     * 
     * @param commandNode
     *            The sub command to add.
     */
    public void addCommand(CommandNode commandNode) {
        commands.put(commandNode.getName(), commandNode);
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

    /**
     * Gather the arguments in the given class, create a assignment meta
     * information object, and add it to the given map of argument names to meta
     * information assignments.
     * 
     * @param arguableClass
     *            The class to harvest for arguments.
     * @param assignment
     *            The map of argument names to meta information objects.
     */
    private static void gatherAssignments(InputOutput io, Class<?> arguableClass, Map<String, Assignment> assignment) {
        for (java.lang.reflect.Method method : arguableClass.getMethods()) {
            if (method.getName().startsWith("add")
                && method.getName().length() != 3
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
                    checkForDuplicates(io, arguableClass, assignment, verbose);
                    Class<?> type =  Primitives.box(method.getParameterTypes()[0]);
                    assignment.put(verbose, new Assignment(new MethodSetter(method, null), INFUSER.getInfuser(type)));
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
                checkForDuplicates(io, arguableClass, assignment, verbose);
                Class<?> type =  Primitives.box(field.getType());
                assignment.put(verbose, new Assignment(new FieldSetter(field), INFUSER.getInfuser(type)));
            }
        }
        // No superclass inspection. The above methods return all public methods
        // and fields.
    }

    /**
     * Check for duplicates. This method extracted to reduce branches and ease
     * testing.
     * 
     * @param io
     *            The I/O bouquet.
     * @param arguableClass
     *            The commandable class being inspected.
     * @param assignment
     *            The map of argument names to assignment meta information.
     * @param name
     *            The argument name.
     */
    private static void checkForDuplicates(InputOutput io, Class<?> arguableClass, Map<String, Assignment> assignment, String name) {
        if (assignment.containsKey(name)) {
            Environment.error(io, CommandNode.class, "duplicateArgument", arguableClass, "name");
        }
    }
}
