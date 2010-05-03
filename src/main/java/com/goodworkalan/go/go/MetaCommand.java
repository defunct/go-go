package com.goodworkalan.go.go;

import java.util.Map;

/**
 * Meta information on the commands available to the currently executing thread.
 * 
 * @author Alan Gutierrez
 */
public interface MetaCommand {
    /**
     * Get the parent command class or null if this is a root command.
     * 
     * @return The parent command class or null.
     */
    public Class<? extends Commandable> getParentCommandClass();

    /**
     * Get the command name.
     * 
     * @return The command name.
     */
    public String getName();

    /**
     * Get a map of the names and types of arguments accepted by this command.
     * 
     * @return The names and types of arguments accepted by this command.
     */
    public Map<String, Class<?>> getArguments();
}
