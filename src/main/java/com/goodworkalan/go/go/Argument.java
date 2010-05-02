package com.goodworkalan.go.go;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applied to the setter methods of argument properties for
 * {@link Commandable} beans. The property must be either a string property or
 * an object that with a public constructor that takes a single string argument.
 * 
 * @author Alan Gutierrez
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD})
public @interface Argument {
    /**
     * The name of the argument. If not specified, the name is dervied form the
     * Java property.
     */
    String value() default "";
}
