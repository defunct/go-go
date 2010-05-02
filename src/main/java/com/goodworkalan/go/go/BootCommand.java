package com.goodworkalan.go.go;

/**
 * Base command for root Jav-a-Go-Go tasks.
 *  
 * @author Alan Gutierrez
 */
@Command
public class BootCommand implements Commandable {
    public void execute(Environment env) {
        System.out.println(env.remaining);
    }
}
