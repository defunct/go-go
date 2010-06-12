package com.goodworkalan.go.go;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applied to {@link Commandable} Beans to specify an command name other
 * than the one derived from the class name.
 * 
 * @author Alan Gutierrez
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The command line as exposed to the command line. This name can contain
     * alphanumeric characters, hyphens and underlines.
     */
    String name() default "";

    /**
     * Whether or not to cache the output of command for a particular command
     * line.
     * 
     * @return True if the command output should be cached.
     */
    boolean cache() default true;

    /**
     * The parent command in the command hierarchy or {@link Commandable} if
     * this is a root command.
     * 
     * @return The parent command.
     */
    Class<? extends Commandable> parent() default Commandable.class;
}
