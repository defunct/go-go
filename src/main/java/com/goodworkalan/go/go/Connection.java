package com.goodworkalan.go.go;

/**
 * A connection element in the domain-specific language that connects command
 * line arguments to tasks.
 * 
 * @author Alan Gutierrez
 */
public class Connection<End> {
    /**
     * Create a sub command with the given name that invokes the given task.
     * 
     * @param name The sub command name.
     * @param task The task.
     * @return A connect language element to specify connection details.
     */
    public Connection<Connection<End>> connect(String name, Class<? extends Task> task) {
        return new Connection<Connection<End>>();
    }
    
    /**
     * Specify an argument to 
     * @param name
     * @param type
     * @param required
     * @return
     */
    public Connection<End> argument(String name, Class<?> type, boolean required) {
        return this;
    }
    
    public Connection<End> arguments(String name, Class<?> type, boolean required) {
        return this;
    }

    public End end() {
        return null;
    }
}
