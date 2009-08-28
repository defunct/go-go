package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.MULTIPLE_TASK_PARENTS;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.goodworkalan.reflective.Method;

/**
 * A wrapper around a task that maps task properites to assignment and parameter
 * conversion strategies for those properties, and collects the sub commands of
 * the task.
 * 
 * @author Alan Gutierrez
 */
public class Responder {
    /** The task class. */
    private final Class<? extends Task> taskClass;

    /** The name of the command. */
    private final String name;

    /** The parent task if any. */
    private final Map<Method, Class<? extends Task>> parent;

    /** A map of verbose argument names to assigment strategies. */
    private final Map<String, Assignment> assgnments;

    /** A map of sub command names to sub command responders. */ 
    private final Map<String, Responder> subCommands;

    /**
     * Create wrapper around the given class that extends <code>Task</code>.
     * 
     * @param taskClass
     *            The task class.
     */
    public Responder(Class<? extends Task> taskClass) {
        Command command = taskClass.getAnnotation(Command.class);
        String name;
        if (command == null) {
            String className = taskClass.getCanonicalName();
            int index = className.lastIndexOf('.');
            if (index != -1) {
                className = className.substring(index + 1);
            }
            name = hyphenate(className);
        } else {
            name = command.value();
        }
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(taskClass, Object.class);
        } catch (IntrospectionException e) {
            throw new GoException(0, e);
        }
        Map<Method, Class<? extends Task>> parent = new HashMap<Method, Class<? extends Task>>();
        Map<String, Assignment> assgnments = new TreeMap<String, Assignment>();
        for (PropertyDescriptor property : info.getPropertyDescriptors()) {
            java.lang.reflect.Method method = property.getWriteMethod();
            if (method != null) {
                Argument argument = method.getAnnotation(Argument.class);
                if (argument != null) {
                    String verbose = argument.value();
                    if (verbose.equals("")) {
                        verbose = hyphenate(property.getName());
                    }
                    Class<?> type =  objectify(property.getPropertyType());
                    if (Task.class.isAssignableFrom(type)) {
                        if (!parent.isEmpty()) {
                            throw new GoException(MULTIPLE_TASK_PARENTS);
                        }
                        parent.put(new Method(method), castTaskClass(type));
                    } else if (type.equals(String.class)) {
                        assgnments.put(verbose, new Assignment(new Method(method), new StringConverter()));
                    } else {
                        Constructor<?> constructor;
                        try {
                            constructor = type.getConstructor(new Class<?>[] { String.class });
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(0, e);
                        }
                        assgnments.put(verbose, new Assignment(new Method(method), new ConstructorConverter(constructor)));
                    }
                }
            }
        }
        this.taskClass = taskClass;
        this.name = name;
        this.parent = parent;
        this.assgnments = assgnments;
        this.subCommands = new TreeMap<String, Responder>();
    }

    /**
     * Cast a class to class that extends <code>Task</code>.
     * 
     * @param taskClass
     *            A class.
     * @return A class that extends <code>Task</code>.
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Task> castTaskClass(Class taskClass) {
        return taskClass;
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

    /**
     * Convert the type into an object type if the type is a primitive type.
     * 
     * @param primitive
     *            A possible primitive type.
     * @return An object type.
     */
    private static Class<?> objectify(Class<?> primitive) {
        if (boolean.class.equals(primitive)) {
            return Boolean.class;
        } else if (byte.class.equals(primitive)) {
            return Byte.class;
        } else if (short.class.equals(primitive)) {
            return Short.class;
        } else if (int.class.equals(primitive)) {
            return Integer.class;
        } else if (long.class.equals(primitive)) {
            return Long.class;
        } else if (double.class.equals(primitive)) {
            return Double.class;
        } else if (float.class.equals(primitive)) {
            return Float.class;
        }
        return primitive;
    }

    /**
     * Get the task class.
     * 
     * @return The task class.
     */
    public Class<? extends Task> getTaskClass() {
        return taskClass;
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
     * Get the parent task.
     * 
     * @return The parent task for null if none.
     */
    public Class<? extends Task> getParentTaskClass() {
        return parent.isEmpty() ? null : parent.values().iterator().next();
    }

    /**
     * Set the parent task of the given task using the parent task setter for
     * the task type warpped in this responder.
     * 
     * @param task
     *            The task.
     * @param parentTask
     *            The parent task.
     */
    public void setParent(Task task, Task parentTask) {
        Method method = parent.keySet().iterator().next();
        try {
            method.invoke(task, new Object[]{ parentTask });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GoException(0, e);
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
     * The responder for the given sub command if any.
     * 
     * @param command
     *            The sub command name.
     * @return The responder or null.
     */
    public Responder getCommand(String command) {
        return subCommands.get(command);
    }

    /**
     * Set the given argument to the given value in the given task.
     * 
     * @param task
     *            The task.
     * @param argument
     *            The argument.
     * @param value
     *            The value.
     */
    public void setArgument(Task task, String argument, String value) {
        Assignment assignment = null;
        if (value == null) {
            if (argument.startsWith("no-")) {
                assignment = assgnments.get(argument.substring(3));
                if (assignment != null) {
                    if (objectify(assignment.getType()).equals(Boolean.class)) {
                        value = "false";
                    } else {
                        assignment = null;
                    }
                }
            }
        }
        if (assignment == null) {
            assignment = assgnments.get(argument);
            if (assignment == null) {
                throw new GoException(0);
            }
            if (value == null && objectify(assignment.getType()).equals(Boolean.class)) {
                value = "true";
            }
        }
        assignment.setValue(task, value);
    }
}
