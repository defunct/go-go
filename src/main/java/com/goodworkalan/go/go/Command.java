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
//FIXME Maybe rename GoGoCommand, so you can reuse Command?
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name() default "";
    
    boolean hidden() default false;
    
    boolean cache() default true;
    
    Class<? extends Commandable> parent() default Commandable.class;
}
