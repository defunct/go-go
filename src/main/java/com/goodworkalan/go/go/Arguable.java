package com.goodworkalan.go.go;

/**
 * A marker interface indicating that the object can be populated from command
 * line arguments.
 * <p>
 * This interface is attached to a Java bean. The bean is constructed and
 * populated with command line arguments that match the bean properties that
 * have been annotated with the {@link Argument} annotation.
 * 
 * @author Alan Gutierrez
 */
public interface Arguable {
}
