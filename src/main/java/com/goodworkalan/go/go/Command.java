package com.goodworkalan.go.go;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applied to {@link Task} Beans to specify an command name other
 * than the one derived from the class name.
 * 
 * @author Alan Gutierrez
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String value();
}
