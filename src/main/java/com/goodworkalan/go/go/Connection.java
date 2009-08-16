package com.goodworkalan.go.go;

/**
 * A connection element in the domain-specific language that connects command
 * line arguments to tasks.
 * 
 * @author Alan Gutierrez
 */
public class Connection<End> {
    public Connection<Connection<End>> connect(String name, Class<? extends Task> task) {
        return new Connection<Connection<End>>();
    }

    public End end() {
        return null;
    }
}
