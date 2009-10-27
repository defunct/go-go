package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Casts.arguableClass;
import static com.goodworkalan.go.go.Casts.objectify;
import static com.goodworkalan.go.go.Casts.outputClass;
import static com.goodworkalan.go.go.Casts.taskClass;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * A wrapper around a task that maps task properties to assignment and parameter
 * conversion strategies for those properties, and collects the sub commands of
 * the task.
 * 
 * @author Alan Gutierrez
 */
class Responder {
    /** The task class. */
    private final Class<? extends Commandable> taskClass;

    /** The name of the command. */
    private final String name;

    /** The parent task if any. */
    private final Class<? extends Commandable> parent;

    /** A map of verbose argument names to assignment strategies. */
    private final Map<String, Assignment> assignments;

    /** A map of properties that consume argument structures. */
    private final Map<Class<? extends Arguable>, Method> arguables;

    /** A map of properties that consume outputs. */
    private final Map<Class<? extends Output>, Method> inputs;
    
    /** A map of properites that emit output. */
    private final Map<Class<? extends Output>, Method> outputs; 

    /** A map of sub command names to sub command responders. */ 
    private final Map<String, Responder> subCommands;
    
    private final Map<String, Class<?>> arguments;

    /**
     * Create wrapper around the given class that extends <code>Task</code>.
     * 
     * @param taskClass
     *            The task class.
     */
    public Responder(Class<? extends Commandable> taskClass) {
        this.arguables = new HashMap<Class<? extends Arguable>, Method>();
        this.inputs = new HashMap<Class<? extends Output>, Method>();
        this.assignments = new TreeMap<String, Assignment>();
        this.outputs = new HashMap<Class<? extends Output>, Method>();
        
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
        gatherNestedAssignments(taskClass, assignments);
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(taskClass, Object.class);
        } catch (IntrospectionException e) {
            throw new GoException(0, e);
        }

        for (PropertyDescriptor property : info.getPropertyDescriptors()) {
            java.lang.reflect.Method read = property.getReadMethod();
            if (read != null) {
                Class<?> type = objectify(property.getPropertyType());
                if (Output.class.isAssignableFrom(type)) {
                    checkPropertyNested(type);
                    outputs.put(outputClass(type), new Method(read));
                }
            }
            java.lang.reflect.Method write = property.getWriteMethod();
            if (write != null) {
                Class<?> type = objectify(property.getPropertyType());
                boolean arguable = Arguable.class.isAssignableFrom(type);
                boolean output = Output.class.isAssignableFrom(type);
                if (arguable) {
                    if (output) {
                        throw new GoException(0);
                    }
                    checkPropertyNested(type);
                    arguables.put(arguableClass(type), new Method(write));
                } else if (output) {
                    checkPropertyNested(type);
                    inputs.put(outputClass(type), new Method(write));
                }
            }
        }

        Map<String, Class<?>> arguments = new HashMap<String, Class<?>>();
        for (Map.Entry<String, Assignment> entry : assignments.entrySet()) {
            arguments.put(entry.getKey(), entry.getValue().getType());
        }

        this.arguments = Collections.unmodifiableMap(arguments);
        this.taskClass = taskClass;
        this.name = name;
        this.parent = parent;
        this.subCommands = new TreeMap<String, Responder>();
    }
    
    private void checkPropertyNested(Class<?> type) {
        if (type.getDeclaringClass() == null) {
            throw new GoException(0);
        }
        if (!Commandable.class.isAssignableFrom(type.getDeclaringClass())) {
            throw new GoException(0);
        }
    }

    private static void gatherNestedAssignments(Class<? extends Commandable> taskClass, Map<String, Assignment> assignments) {
        for (Class<?> nestedClass : taskClass.getDeclaredClasses()) {
            if (Arguable.class.isAssignableFrom(nestedClass)) {
                gatherAssignments(arguableClass(nestedClass), assignments);
            }
        }
        Class<?> superclass = taskClass.getSuperclass();
        if (Commandable.class.isAssignableFrom(superclass)) {
            gatherNestedAssignments(taskClass(superclass), assignments);
        }
    }

    private static void gatherAssignments(Class<? extends Arguable> arguableClass, Map<String, Assignment> assignment) {
        ReflectiveFactory reflectiveFactory = new ReflectiveFactory();
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
                    Class<?> type =  objectify(method.getParameterTypes()[0]);
                    if (type.equals(String.class)) {
                        assignment.put(verbose, new Assignment(arguableClass, new Method(method), new StringConverter()));
                    } else {
                        assignment.put(verbose, new Assignment(arguableClass, new Method(method), new ConstructorConverter(reflectiveFactory, type)));
                    }
                }
            }
        }
        Class<?> superclass = arguableClass.getSuperclass();
        if (Arguable.class.isAssignableFrom(superclass)) {
            gatherAssignments(arguableClass(superclass), assignment);
        }
    }

    /**
     * Convert a camel case name into a command line hyphenated name by
     * replacing capital letters with lower case letters preceeded by hyphens.
     * 
     * @param camelCase
     *            The camel case name.
     * @return A hyphenated name.
     */
    private static String hyphenate(String camelCase) {
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
    
    public void checkEndlessRecursion(Map<Class<? extends Commandable>, Responder> tasks, Set<Class<? extends Commandable>> seen) {
        Set<Class<? extends Commandable>> subSeen = new HashSet<Class<? extends Commandable>>(seen);
        subSeen.add(taskClass);
        for (Class<? extends Output> intputClass : inputs.keySet()) {
            Class<? extends Arguable> taskClass = taskClass(intputClass.getDeclaringClass());
            if (subSeen.contains(taskClass)) {
                throw new GoException(0);
            }
            Responder responder = tasks.get(taskClass);
            if (responder == null) {
                throw new GoException(0);
            }
            responder.checkEndlessRecursion(tasks, subSeen);
        }
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
        assert responder.getParentTaskClass().equals(getTaskClass());
        subCommands.put(responder.getName(), responder);
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
    public Class<? extends Commandable> getTaskClass() {
        return taskClass;
    }

    /**
     * Get the parent task.
     * 
     * @return The parent task for null if none.
     */
    public Class<? extends Commandable> getParentTaskClass() {
        return parent;
    }

    public Map<String, Assignment> getAssignments() {
        return assignments;
    }

    public Set<Class<? extends Arguable>> getArguables() {
        return arguables.keySet();
    }
    
    public void setArguable(Commandable task, Class<? extends Arguable> arguable, Arguable value) {
        try {
            arguables.get(arguable).invoke(task, value);
        } catch (ReflectiveException e) {
            throw new GoException(0, e);
        }
    }

    public Set<Class<? extends Output>> getInputs() {
        return inputs.keySet();
    }
    
    public void setInput(Commandable task, Class<? extends Output> input, Output value) {
        try {
            inputs.get(input).invoke(task, value);
        } catch (ReflectiveException e) {
            throw new GoException(0, e);
        }
    }
    
    public Set<Class<? extends Output>> getOutputs() {
        return outputs.keySet();
    }
    
    public Output getOutput(Commandable task, Class<? extends Output> output) {
        try {
            return (Output) outputs.get(output).invoke(task);
        } catch (ReflectiveException e) {
            throw new GoException(0, e);
        }
    }
    
    /**
     * The responder for the given sub command if any.
     * 
     * @param command
     *            The sub command name.
     * @return The responder or null.
     */
    public Responder getCommand(String command) {
        return subCommands.get(command);
    }
    
    public Collection<Responder> getCommands() {
        return subCommands.values();
    }

    public Map<String, Class<?>> getArguments() {
        return arguments;
    }
}
