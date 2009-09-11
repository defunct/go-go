package com.goodworkalan.go.go;

import java.util.Map;

/**
 * Meta information for a task.
 * 
 * @author Alan Gutierrez
 */
public interface TaskInfo {
    /**
     * Get the task class.
     * 
     * @return The task class.
     */
    public Class<? extends Task> getTaskClass();

    /**
     * Return a map of arguments to argument types.
     * 
     * @return A map of arguments to argument types.
     */
    public Map<String, Class<?>> getArguments();
}
