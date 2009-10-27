package com.goodworkalan.go.go;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applied to the setter methods of argument properties for
 * {@link Commandable} Beans. The property must be either a <code>String</code>
 * property or an object that with a constructor that takes a single
 * <code>String</code> argument.
 * 
 * @author Alan Gutierrez
 */
// FIXME Maybe rename GoGoArgument, so you can reuse Command?
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Argument {
    String value() default "";
}
