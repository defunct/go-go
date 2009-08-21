package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.MULTIPLE_TASK_PARENTS;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Responder {
    /** The task class. */
    private final Class<? extends Task> taskClass;

    /** The name of the command. */
    private final String name;

    /** The parent task if any. */
    private final Map<Method, Class<? extends Task>> parent;

    private final Map<String, Assignment> assgnments;

    private final Map<String, Responder> commands;

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
            Method method = property.getWriteMethod();
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
                        parent.put(method, castTaskClass(type));
                    } else if (type.equals(String.class)) {
                        assgnments.put(verbose, new Assignment(method, new StringConverter()));
                    } else {
                        Constructor<?> constructor;
                        try {
                            constructor = type.getConstructor(new Class<?>[] { String.class });
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(0, e);
                        }
                        assgnments.put(verbose, new Assignment(method, new ConstructorConverter(constructor)));
                    }
                }
            }
        }
        this.taskClass = taskClass;
        this.name = name;
        this.parent = parent;
        this.assgnments = assgnments;
        this.commands = new TreeMap<String, Responder>();
    }

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
    
    public void addCommand(Responder responder) {
        commands.put(responder.getName(), responder);
    }
    
    public Responder getCommand(String command) {
        return commands.get(command);
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
