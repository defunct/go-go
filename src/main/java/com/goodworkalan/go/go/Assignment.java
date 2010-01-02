package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.Casts.arguableClass;
import static com.goodworkalan.go.go.Casts.objectify;
import static com.goodworkalan.go.go.Casts.taskClass;
import static com.goodworkalan.go.go.GoException.ASSIGNMENT_EXCEPTION_THROWN;
import static com.goodworkalan.go.go.GoException.ASSIGNMENT_FAILED;

import java.lang.reflect.Modifier;
import java.util.Map;

import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * Assigns a command line argument to a property in a {@link Arguable} Bean.
 * 
 * @author Alan Gutierrez
 */
public class Assignment {
    /** The class that declared the assignment. */
    private final Class<? extends Arguable> declaringClass;
    
    /** The method to set the task property. */
    private final Method setter;

    /** The converter for the string property. */
    private final Converter converter;

    /**
     * Create an assignment that will set the property identified by the given
     * setter method with a string value converted with the given converter.
     * 
     * @param declaringClass
     *            The class that declared the assignment.
     * @param setter
     *            The property setter.
     * @param converter
     *            The string converter.
     */
    Assignment(Class<? extends Arguable> declaringClass, Method setter, Converter converter) {
        this.declaringClass = declaringClass;
        this.setter = setter;
        this.converter = converter;
    }

    public Object convertValue(String value) {
        return converter.convert(value);
    }

    /**
     * Set the property of the task that is defined by this assignment to the
     * value represented by the given string.
     * 
     * @param arguable
     *            The task.
     * @param value
     *            The string value.
     */
    public void setValue(Arguable arguable, Object value) {
        try {
            setter.invoke(arguable, value);
        } catch (ReflectiveException e) {
            if (e.getCode() == ReflectiveException.INVOCATION_TARGET) {
                throw new GoException(ASSIGNMENT_EXCEPTION_THROWN, e, getType(), setter.getNative().getName());
            }
            throw new GoException(ASSIGNMENT_FAILED, e, getType(), setter.getNative().getName());
        }
    }

    /**
     * Get the type of the declaring class.
     * 
     * @return The type of the declaring class.
     */
    public Class<? extends Arguable> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Get the assignment type.
     * 
     * @return The assignment type.
     */
    public Class<?> getType() {
        return setter.getNative().getParameterTypes()[0];
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

    public static void gatherNestedAssignments(Class<? extends Commandable> taskClass, Map<String, Assignment> assignments) {
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

    public static void gatherAssignments(Class<? extends Arguable> arguableClass, Map<String, Assignment> assignment) {
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
}
